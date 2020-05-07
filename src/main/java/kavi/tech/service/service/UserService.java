package kavi.tech.service.service;

import io.vertx.core.json.JsonObject;
import kavi.tech.service.mybatis.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Single;
import tech.kavi.vs.mybatis.AsyncHelperKt;

import java.util.ArrayList;
import java.util.List;

//@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 查询所有用户结果
     * */
    public Single<List<JsonObject>> listAll() {
        return AsyncHelperKt.singleAsync(userMapper::list).map(it -> {
            List<JsonObject> list = new ArrayList();
            it.iterator().forEachRemaining(user -> list.add(user.toJson()));
            return list;
        });
    }
}
