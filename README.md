# mybatis-redis
应用redis作为mybatis的二级缓存,目前只支持单机版的redis实例


##功能及原理
扩展mybatis的二级缓存层，应用redis作为mybatis的二级缓存。另该扩展组件，支持更细粒度的缓存。所谓更细粒度的缓存指的是，支持表级的缓存。mybatis本身的缓存设计，只支持单个mapper级别的缓存(关于mybatis的缓存设计，请参见mybatis源代码)。那么在mapper中出现关联查询，则会出现缓存不致现象，为了解决缓存不一致，需要进行表级数据缓存。该功能依赖于Cachekey的构造，从Cachekey实例中应用sql解析相关的数据库表，然后存储数据表与cachekey实例的关系。

本组件支持自定义的对象序列化，只需要实现 ObjectSerializer接口，在配置文件中
serialze_clazz=org.mybatis.cache.redis.support.HessianSerialize
进行如下配置即可;


##数据结构

key -value

mapperid -该mapper下的所有key集合

tablename -　与该table相关的所有key集合

 当执行单个mapper上的缓存管理时，首先根据mapperid可以找到所有与该mapper相关的所有key，尽而可以清除所有的缓存;
 第二步根据第一步找到的所有key可以解析出所有的表名，从而可以找到与这些表名相关的所有key，根据这组key就可以进行第二次缓存的清理，
 (此步有可能缓存的清理过多，请慎重选择，开启该功能是配置 use.grained.cache=true,默认是关闭即use.grained.cache=false)

 
 ###配置文件加载
 
 组件启动默认会先加载jar包的default-rediscache.properties,
 而后获取系统参数redis.config.path对应的配置文件,
 最后加载放在classpath下的rediscache.properties文件;
 后加载的属性会覆盖前面加载的同名属性值