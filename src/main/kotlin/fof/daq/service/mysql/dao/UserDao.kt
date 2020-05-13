package fof.daq.service.mysql.dao

import io.vertx.core.logging.Logger
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import io.vertx.rxjava.ext.sql.SQLConnection
import fof.daq.service.common.extension.logger
import fof.daq.service.common.strategry.HashStrategy
import fof.daq.service.mysql.component.AbstractDao
import fof.daq.service.mysql.entity.User
import fof.daq.service.mysql.component.SQL
import io.vertx.ext.web.handler.impl.HttpStatusException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.io.Serializable
import rx.Single

@Repository
class UserDao @Autowired constructor(
    private val client: AsyncSQLClient,
    private val strategy: HashStrategy
) : AbstractDao<User>(client) {
    override val log: Logger = logger(this::class)

    /**
     * 用户登入搜索
     * */
    fun login(where: List<Serializable>): Single<User> {
        return this.one(SQL.init { SELECT("*"); FROM(User.tableName); WHERES(where) })
    }

    /**
     * 创建用户
     * */
    fun save(user: User): Single<User> {
        val username = user.username
        val password = user.password
        if (user.id == null && password == null) {
            return Single.error(NullPointerException("Password is not null"))
        }
        if (username.isNullOrEmpty()) {
            return Single.error(NullPointerException("Username is not null"))
        }
        /*防止外部更新的数据（设为null不处理的数据）*/
        user.password = null
        user.passwordSalt = null
        /*设置新密码*/
        password?.apply {
            val version = -1 // 默认版本
            val passwordSalt = strategy.generateSalt()
            user.password = strategy.computeHash(this, passwordSalt, version)
            user.passwordSalt = passwordSalt
        }
        /* 验证用户名唯一存在 */
        val sql = SQL.init {
            SELECT("id")
            FROM(User.tableName)
            WHERE(Pair("username", username))
        }
        return this.one(sql) { conn, result ->
            when (result != null && result.rows.isNotEmpty()) {
                true -> Single.error(IllegalAccessError("Username are already exist"))
                else -> insert(conn, user)
            }
        }
    }

    /**
     * 新增记录
     * */
    private fun insert(conn: SQLConnection, user: User): Single<User> {
        val sql = SQL.init {
            INSERT_INTO(user.tableName())
            user.preInsert().forEach { t, u -> VALUES(t, u) }
        }

        println("insert user:$user")
        println("--------------------sql:" + sql)
        return this.update(conn, sql).map {
            user.apply { this.id = it.keys.getLong(0) }
        }
    }

    /**
     * 更新用户密码
     * */

    private fun updatePasswordAndPasswordSalt(conn: SQLConnection, user: User): Single<User> {

        val sql = " UPDATE `${user.tableName()}` \n" +
                "\tSET `password`  = '${user.password}', \n" +
                "\t`password_salt` = '${user.passwordSalt}',\n" +
                "\t`updated_at` = ${System.currentTimeMillis()}\n" +
                "\tWHERE `username` = '${user.username}' \n" +
                "\tand deleted_at = 0;"

        log.info("update user:${user.username}")
        return this.update(conn, sql).map {
            log.info("update user:${user.username} success!")
            user.apply { this.id = it.keys.getLong(0) }
        }
    }

    /**
     * 修改密码
     * */
    fun updatePassword(userName: String, oldPassword: String, newPassword: String): Single<User> {
        val username = userName
        val password = newPassword
        if (oldPassword.isNullOrEmpty() || password.isNullOrEmpty()) {
            return Single.error(NullPointerException("Password is not null"))
        }
        if (username.isNullOrEmpty()) {
            return Single.error(NullPointerException("Username is not null"))

        }
        var result = false
       return this.login(listOf(Pair("username", username))).flatMap { user ->
            log.info("====================修改密码 ：$user")
            user ?: throw HttpStatusException(403, NullPointerException("User is null"))
            val userPassword = user.password
            val passwordSalt = user.passwordSalt
            when {
                userPassword == null -> throw HttpStatusException(403, NullPointerException("User password is null"))
                passwordSalt == null -> throw HttpStatusException(403, NullPointerException("User salt is null"))
                else -> {
                    // 查询出来的密码
                    val version = strategy.version(userPassword)
                    val hashedPassword = strategy.computeHash(oldPassword, passwordSalt, version)
                    if (strategy.isEqual(userPassword, hashedPassword)) {
                        readyToUpdatePasswordAndPasswordSalt(username, password)
                    } else {
                        log.info("用户：[${user.username}] 输入的原密码错误！")
                        Single.error(ExceptionInInitializerError("原密码错误！"))

                    }
                }
            }

        }


    }

    /**
     *  更新密码及盐值
     */
    fun readyToUpdatePasswordAndPasswordSalt(username: String, password: String): Single<User> {
        log.info("===========更新用户[$username]的密码及盐值===================")
        // 允许修改
        val newUser = User()
        /*防止外部更新的数据（设为null不处理的数据）*/
        newUser.username = username
        newUser.password = null
        newUser.passwordSalt = null
        /*设置新密码*/
        password?.apply {
            val version = -1 // 默认版本
            val newPasswordSalt = strategy.generateSalt()
            newUser.password = strategy.computeHash(this, newPasswordSalt, version)
            newUser.passwordSalt = newPasswordSalt
        }

        /* 验证用户名唯一存在 */
        val querySql = SQL.init {
            SELECT("id")
            FROM(User.tableName)
            WHERE(Pair("username", newUser.username))
        }

        return this.one(querySql) { conn, result ->
            when (result != null && result.rows.isNotEmpty()) {
                false -> Single.error(IllegalAccessError("Username are not exist"))
                else -> updatePasswordAndPasswordSalt(conn, newUser)
            }
        }
    }

}
