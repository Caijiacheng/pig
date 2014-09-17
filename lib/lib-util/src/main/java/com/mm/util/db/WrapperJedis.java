package com.mm.util.db;

import redis.clients.jedis.Jedis;

public class WrapperJedis extends Jedis{

	public WrapperJedis(String host, int port) {
		super(host, port);
	}
	
	@Override
	public void close() {
		if (!AbsDBHandle.s_conn_share_mode.get())
			super.close();
	}
	
//
//	
//	public WrapperJedis(Jedis jedis) {
//		this.jedis = jedis;
//	}
//	
//	@Override
//	public String clusterNodes() {
//		return jedis.clusterNodes();
//	}
//
//	@Override
//	public String clusterMeet(String ip, int port) {
//		return jedis.clusterMeet(ip, port);
//	}
//
//	@Override
//	public String clusterAddSlots(int... slots) {
//		return jedis.clusterAddSlots(slots);
//	}
//
//	@Override
//	public String clusterDelSlots(int... slots) {
//		return jedis.clusterDelSlots(slots);
//	}
//
//	@Override
//	public String clusterInfo() {
//		return jedis.clusterInfo();
//	}
//
//	@Override
//	public List<String> clusterGetKeysInSlot(int slot, int count) {
//		return jedis.clusterGetKeysInSlot(slot, count);
//	}
//
//	@Override
//	public String clusterSetSlotNode(int slot, String nodeId) {
//		return jedis.clusterSetSlotNode(slot, nodeId);
//	}
//
//	@Override
//	public String clusterSetSlotMigrating(int slot, String nodeId) {
//		return jedis.clusterSetSlotMigrating(slot, nodeId);
//	}
//
//	@Override
//	public String clusterSetSlotImporting(int slot, String nodeId) {
//		return jedis.clusterSetSlotImporting(slot, nodeId);
//	}
//
//	@Override
//	public String clusterSetSlotStable(int slot) {
//		return jedis.clusterSetSlotStable(slot);
//	}
//
//	@Override
//	public String clusterForget(String nodeId) {
//		return jedis.clusterForget(nodeId);
//	}
//
//	@Override
//	public String clusterFlushSlots() {
//		return jedis.clusterFlushSlots();
//	}
//
//	@Override
//	public Long clusterKeySlot(String key) {
//		return jedis.clusterKeySlot(key);
//	}
//
//	@Override
//	public Long clusterCountKeysInSlot(int slot) {
//		return jedis.clusterCountKeysInSlot(slot);
//	}
//
//	@Override
//	public String clusterSaveConfig() {
//		return jedis.clusterSaveConfig();
//	}
//
//	@Override
//	public String clusterReplicate(String nodeId) {
//		return jedis.clusterReplicate(nodeId);
//	}
//
//	@Override
//	public List<String> clusterSlaves(String nodeId) {
//		return jedis.clusterSlaves(nodeId);
//	}
//
//	@Override
//	public String clusterFailover() {
//		return jedis.clusterFailover();
//	}
//
//	@Override
//	public String ping() {
//		return jedis.ping();
//	}
//
//	@Override
//	public String quit() {
//		return jedis.quit();
//	}
//
//	@Override
//	public String flushDB() {
//		return jedis.flushDB();
//	}
//
//	@Override
//	public Long dbSize() {
//		return jedis.dbSize();
//	}
//
//	@Override
//	public String select(int index) {
//		return jedis.select(index);
//	}
//
//	@Override
//	public String flushAll() {
//		return jedis.flushAll();
//	}
//
//	@Override
//	public String auth(String password) {
//		return jedis.auth(password);
//	}
//
//	@Override
//	public String save() {
//		return jedis.save();
//	}
//
//	@Override
//	public String bgsave() {
//		return jedis.bgsave();
//	}
//
//	@Override
//	public String bgrewriteaof() {
//		return jedis.bgrewriteaof();
//	}
//
//	@Override
//	public Long lastsave() {
//		return jedis.lastsave();
//	}
//
//	@Override
//	public String shutdown() {
//		return jedis.shutdown();
//	}
//
//	@Override
//	public String info() {
//		return jedis.info();
//	}
//
//	@Override
//	public String info(String section) {
//		return jedis.info(section);
//	}
//
//	@Override
//	public String slaveof(String host, int port) {
//		return jedis.slaveof(host, port);
//	}
//
//	@Override
//	public String slaveofNoOne() {
//		return jedis.slaveofNoOne();
//	}
//
//	@Override
//	public Long getDB() {
//		return jedis.getDB();
//	}
//
//	@Override
//	public String debug(DebugParams params) {
//		return jedis.debug(params);
//	}
//
//	@Override
//	public String configResetStat() {
//		return jedis.configResetStat();
//	}
//
//	@Override
//	public Long waitReplicas(int replicas, long timeout) {
//		return jedis.waitReplicas(replicas, timeout);
//	}
//
//	@Override
//	public Object eval(String script, int keyCount, String... params) {
//		return jedis.eval(script, keyCount, params);
//	}
//
//	@Override
//	public Object eval(String script, List<String> keys, List<String> args) {
//		return jedis.eval(script, keys, args);
//	}
//
//	@Override
//	public Object eval(String script) {
//		return jedis.eval(script);
//	}
//
//	@Override
//	public Object evalsha(String script) {
//		return jedis.evalsha(script);
//	}
//
//	@Override
//	public Object evalsha(String sha1, List<String> keys, List<String> args) {
//		return jedis.evalsha(sha1, keys, args);
//	}
//
//	@Override
//	public Object evalsha(String sha1, int keyCount, String... params) {
//		return jedis.evalsha(sha1, keyCount, params);
//	}
//
//	@Override
//	public Boolean scriptExists(String sha1) {
//		return jedis.scriptExists(sha1);
//	}
//
//	@Override
//	public List<Boolean> scriptExists(String... sha1) {
//		return jedis.scriptExists(sha1);
//	}
//
//	@Override
//	public String scriptLoad(String script) {
//		return jedis.scriptLoad(script);
//	}
//
//	@Override
//	public List<String> configGet(String pattern) {
//		return jedis.configGet(pattern);
//	}
//
//	@Override
//	public String configSet(String parameter, String value) {
//		return jedis.configSet(parameter, value);
//	}
//
//	@Override
//	public String slowlogReset() {
//		return jedis.slowlogReset();
//	}
//
//	@Override
//	public Long slowlogLen() {
//		return jedis.slowlogLen();
//	}
//
//	@Override
//	public List<Slowlog> slowlogGet() {
//		return jedis.slowlogGet();
//	}
//
//	@Override
//	public List<Slowlog> slowlogGet(long entries) {
//		return jedis.slowlogGet(entries);
//	}
//
//	@Override
//	public Long objectRefcount(String string) {
//		return jedis.objectRefcount(string);
//	}
//
//	@Override
//	public String objectEncoding(String string) {
//		return jedis.objectEncoding(string);
//	}
//
//	@Override
//	public Long objectIdletime(String string) {
//		return jedis.objectIdletime(string);
//	}
//
//	@Override
//	public Long del(String... keys) {
//		return jedis.del(keys);
//	}
//
//	@Override
//	public List<String> blpop(int timeout, String... keys) {
//		return jedis.blpop(timeout, keys);
//	}
//
//	@Override
//	public List<String> brpop(int timeout, String... keys) {
//		return jedis.brpop(timeout, keys);
//	}
//
//	@Override
//	public List<String> blpop(String... args) {
//		return jedis.blpop(args);
//	}
//
//	@Override
//	public List<String> brpop(String... args) {
//		return jedis.brpop(args);
//	}
//
//	@Override
//	public Set<String> keys(String pattern) {
//		return jedis.keys(pattern);
//	}
//
//	@Override
//	public List<String> mget(String... keys) {
//		return jedis.mget(keys);
//	}
//
//	@Override
//	public String mset(String... keysvalues) {
//		return jedis.mset(keysvalues);
//	}
//
//	@Override
//	public Long msetnx(String... keysvalues) {
//		return jedis.msetnx(keysvalues);
//	}
//
//	@Override
//	public String rename(String oldkey, String newkey) {
//		
//		return jedis.rename(oldkey, newkey);
//	}
//
//	@Override
//	public Long renamenx(String oldkey, String newkey) {
//		return jedis.renamenx(oldkey, newkey);
//	}
//
//	@Override
//	public String rpoplpush(String srckey, String dstkey) {
//		return jedis.rpoplpush(srckey, dstkey);
//	}
//
//	@Override
//	public Set<String> sdiff(String... keys) {
//		return jedis.sdiff(keys);
//	}
//
//	@Override
//	public Long sdiffstore(String dstkey, String... keys) {
//		return jedis.sdiffstore(dstkey, keys);
//	}
//
//	@Override
//	public Set<String> sinter(String... keys) {
//		return jedis.sinter(keys);
//	}
//
//	@Override
//	public Long sinterstore(String dstkey, String... keys) {
//		return jedis.sinterstore(dstkey, keys);
//	}
//
//	@Override
//	public Long smove(String srckey, String dstkey, String member) {
//		return jedis.smove(srckey, dstkey, member);
//	}
//
//	@Override
//	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
//		return jedis.sort(key, sortingParameters, dstkey);
//	}
//
//	@Override
//	public Long sort(String key, String dstkey) {
//		return jedis.sort(key, dstkey);
//	}
//
//	@Override
//	public Set<String> sunion(String... keys) {
//		return jedis.sunion(keys);
//	}
//
//	@Override
//	public Long sunionstore(String dstkey, String... keys) {
//		return jedis.sunionstore(dstkey, keys);
//	}
//
//	@Override
//	public String watch(String... keys) {
//		return jedis.watch(keys);
//	}
//
//	@Override
//	public String unwatch() {
//		return jedis.unwatch();
//	}
//
//	@Override
//	public Long zinterstore(String dstkey, String... sets) {
//		return jedis.zinterstore(dstkey, sets);
//	}
//
//	@Override
//	public Long zinterstore(String dstkey, ZParams params, String... sets) {
//		return jedis.zinterstore(dstkey, params, sets);
//	}
//
//	@Override
//	public Long zunionstore(String dstkey, String... sets) {
//		return jedis.zunionstore(dstkey, sets);
//	}
//
//	@Override
//	public Long zunionstore(String dstkey, ZParams params, String... sets) {
//		return jedis.zunionstore(dstkey, params, sets);
//	}
//
//	@Override
//	public String brpoplpush(String source, String destination, int timeout) {
//		return jedis.brpoplpush(source, destination, timeout);
//	}
//
//	@Override
//	public Long publish(String channel, String message) {
//		return jedis.publish(channel, message);
//	}
//
//	@Override
//	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
//		jedis.subscribe(jedisPubSub, channels);
//	}
//
//	@Override
//	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
//		jedis.psubscribe(jedisPubSub, patterns);
//	}
//
//	@Override
//	public String randomKey() {
//		return jedis.randomKey();
//	}
//
//	@Override
//	public Long bitop(BitOP op, String destKey, String... srcKeys) {
//		return jedis.bitop(op, destKey, srcKeys);
//	}
//
//	@Override
//	@Deprecated
//	public
//	ScanResult<String> scan(int cursor) {
////		return jedis.scan(cursor);
//		Preconditions.checkNotNull(null);
//		return null;
//	}
//
//	@Override
//	public ScanResult<String> scan(String cursor) {
//		return jedis.scan(cursor);
//	}
//
//	@Override
//	public String pfmerge(String destkey, String... sourcekeys) {
//		return jedis.pfmerge(destkey, sourcekeys);
//	}
//
//	@Override
//	public long pfcount(String... keys) {
//		return jedis.pfcount(keys);
//	}
//
//	@Override
//	public String set(String key, String value) {
//		return jedis.set(key, value);
//	}
//
//	@Override
//	public String set(String key, String value, String nxxx, String expx,
//			long time) {
//		return jedis.set(key, value, nxxx, expx, time);
//	}
//
//	@Override
//	public String get(String key) {
//		return jedis.get(key);
//	}
//
//	@Override
//	public Boolean exists(String key) {
//		return jedis.exists(key);
//	}
//
//	@Override
//	public Long persist(String key) {
//		return jedis.persist(key);
//	}
//
//	@Override
//	public String type(String key) {
//		return jedis.type(key);
//	}
//
//	@Override
//	public Long expire(String key, int seconds) {
//		return jedis.expire(key, seconds);
//	}
//
//	@Override
//	public Long expireAt(String key, long unixTime) {
//		return jedis.expireAt(key, unixTime);
//	}
//
//	@Override
//	public Long ttl(String key) {
//		return jedis.ttl(key);
//	}
//
//	@Override
//	public Boolean setbit(String key, long offset, boolean value) {
//		return jedis.setbit(key, offset, value);
//	}
//
//	@Override
//	public Boolean setbit(String key, long offset, String value) {
//		return jedis.setbit(key, offset, value);
//	}
//
//	@Override
//	public Boolean getbit(String key, long offset) {
//		return jedis.getbit(key, offset);
//	}
//
//	@Override
//	public Long setrange(String key, long offset, String value) {
//		return jedis.setrange(key, offset, value);
//	}
//
//	@Override
//	public String getrange(String key, long startOffset, long endOffset) {
//		return jedis.getrange(key, startOffset, endOffset);
//	}
//
//	@Override
//	public String getSet(String key, String value) {
//		return jedis.getSet(key, value);
//	}
//
//	@Override
//	public Long setnx(String key, String value) {
//		return jedis.setnx(key, value);
//	}
//
//	@Override
//	public String setex(String key, int seconds, String value) {
//		return jedis.setex(key, seconds, value);
//	}
//
//	@Override
//	public Long decrBy(String key, long integer) {
//		return jedis.decrBy(key, integer);
//	}
//
//	@Override
//	public Long decr(String key) {
//		return jedis.decr(key);
//	}
//
//	@Override
//	public Long incrBy(String key, long integer) {
//		return jedis.incrBy(key, integer);
//	}
//
//	@Override
//	public Long incr(String key) {
//		return jedis.incr(key);
//	}
//
//	@Override
//	public Long append(String key, String value) {
//		return jedis.append(key, value);
//	}
//
//	@Override
//	public String substr(String key, int start, int end) {
//		return jedis.substr(key, start, end);
//	}
//
//	@Override
//	public Long hset(String key, String field, String value) {
//		return jedis.hset(key, field, value);
//	}
//
//	@Override
//	public String hget(String key, String field) {
//		return jedis.hget(key, field);
//	}
//
//	@Override
//	public Long hsetnx(String key, String field, String value) {
//		return jedis.hsetnx(key, field, value);
//	}
//
//	@Override
//	public String hmset(String key, Map<String, String> hash) {
//		return jedis.hmset(key, hash);
//	}
//
//	@Override
//	public List<String> hmget(String key, String... fields) {
//		return jedis.hmget(key, fields);
//	}
//
//	@Override
//	public Long hincrBy(String key, String field, long value) {
//		return jedis.hincrBy(key, field, value);
//	}
//
//	@Override
//	public Boolean hexists(String key, String field) {
//		return jedis.hexists(key, field);
//	}
//
//	@Override
//	public Long hdel(String key, String... field) {
//		return jedis.hdel(key, field);
//	}
//
//	@Override
//	public Long hlen(String key) {
//		return jedis.hlen(key);
//	}
//
//	@Override
//	public Set<String> hkeys(String key) {
//		return jedis.hkeys(key);
//	}
//
//	@Override
//	public List<String> hvals(String key) {
//		return jedis.hvals(key);
//	}
//
//	@Override
//	public Map<String, String> hgetAll(String key) {
//		return jedis.hgetAll(key);
//	}
//
//	@Override
//	public Long rpush(String key, String... string) {
//		return jedis.rpush(key, string);
//	}
//
//	@Override
//	public Long lpush(String key, String... string) {
//		return jedis.lpush(key, string);
//	}
//
//	@Override
//	public Long llen(String key) {
//		return jedis.llen(key);
//	}
//
//	@Override
//	public List<String> lrange(String key, long start, long end) {
//		return jedis.lrange(key, start, end);
//	}
//
//	@Override
//	public String ltrim(String key, long start, long end) {
//		return jedis.ltrim(key, start, end);
//	}
//
//	@Override
//	public String lindex(String key, long index) {
//		return jedis.lindex(key, index);
//	}
//
//	@Override
//	public String lset(String key, long index, String value) {
//		return jedis.lset(key, index, value);
//	}
//
//	@Override
//	public Long lrem(String key, long count, String value) {
//		return jedis.lrem(key, count, value);
//	}
//
//	@Override
//	public String lpop(String key) {
//		return jedis.lpop(key);
//	}
//
//	@Override
//	public String rpop(String key) {
//		return jedis.rpop(key);
//	}
//
//	@Override
//	public Long sadd(String key, String... member) {
//		return jedis.sadd(key, member);
//	}
//
//	@Override
//	public Set<String> smembers(String key) {
//		return jedis.smembers(key);
//	}
//
//	@Override
//	public Long srem(String key, String... member) {
//		return jedis.srem(key, member);
//	}
//
//	@Override
//	public String spop(String key) {
//		return jedis.spop(key);
//	}
//
//	@Override
//	public Long scard(String key) {
//		return jedis.scard(key);
//	}
//
//	@Override
//	public Boolean sismember(String key, String member) {
//		return jedis.sismember(key, member);
//	}
//
//	@Override
//	public String srandmember(String key) {
//		return jedis.srandmember(key);
//	}
//
//	@Override
//	public Long strlen(String key) {
//		return jedis.strlen(key);
//	}
//
//	@Override
//	public Long zadd(String key, double score, String member) {
//		return jedis.zadd(key, score, member);
//	}
//
//	@Override
//	public Long zadd(String key, Map<String, Double> scoreMembers) {
//		return jedis.zadd(key, scoreMembers);
//	}
//
//	@Override
//	public Set<String> zrange(String key, long start, long end) {
//		return jedis.zrange(key, start, end);
//	}
//
//	@Override
//	public Long zrem(String key, String... member) {
//		return jedis.zrem(key, member);
//	}
//
//	@Override
//	public Double zincrby(String key, double score, String member) {
//		return jedis.zincrby(key, score, member);
//	}
//
//	@Override
//	public Long zrank(String key, String member) {
//		return jedis.zrank(key, member);
//	}
//
//	@Override
//	public Long zrevrank(String key, String member) {
//		return jedis.zrevrank(key, member);
//	}
//
//	@Override
//	public Set<String> zrevrange(String key, long start, long end) {
//		return jedis.zrevrange(key, start, end);
//	}
//
//	@Override
//	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
//		return jedis.zrangeWithScores(key, start, end);
//	}
//
//	@Override
//	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
//		return jedis.zrevrangeWithScores(key, start, end);
//	}
//
//	@Override
//	public Long zcard(String key) {
//		return jedis.zcard(key);
//	}
//
//	@Override
//	public Double zscore(String key, String member) {
//		return jedis.zscore(key, member);
//	}
//
//	@Override
//	public List<String> sort(String key) {
//		return jedis.sort(key);
//	}
//
//	@Override
//	public List<String> sort(String key, SortingParams sortingParameters) {
//		return jedis.sort(key, sortingParameters);
//	}
//
//	@Override
//	public Long zcount(String key, double min, double max) {
//		return jedis.zcount(key, min, max);
//	}
//
//	@Override
//	public Long zcount(String key, String min, String max) {
//		return jedis.zcount(key, min, max);
//	}
//
//	@Override
//	public Set<String> zrangeByScore(String key, double min, double max) {
//		return jedis.zrangeByScore(key, min, max);
//	}
//
//	@Override
//	public Set<String> zrangeByScore(String key, String min, String max) {
//		return jedis.zrangeByScore(key, min, max);
//	}
//
//	@Override
//	public Set<String> zrevrangeByScore(String key, double max, double min) {
//		return jedis.zrevrangeByScore(key, max, min);
//	}
//
//	@Override
//	public Set<String> zrangeByScore(String key, double min, double max,
//			int offset, int count) {
//		return jedis.zrangeByScore(key, min, max, offset, count);
//	}
//
//	@Override
//	public Set<String> zrevrangeByScore(String key, String max, String min) {
//		return jedis.zrevrangeByScore(key, max, min);
//	}
//
//	@Override
//	public Set<String> zrangeByScore(String key, String min, String max,
//			int offset, int count) {
//		return jedis.zrangeByScore(key, min, max, offset, count);
//	}
//
//	@Override
//	public Set<String> zrevrangeByScore(String key, double max, double min,
//			int offset, int count) {
//		
//		return jedis.zrevrangeByScore(key, max, min, offset, count);
//	}
//
//	@Override
//	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
//		return jedis.zrangeByScoreWithScores(key, min, max);
//	}
//
//	@Override
//	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
//			double min) {
//		return jedis.zrevrangeByScoreWithScores(key, max, min);
//	}
//
//	@Override
//	public Set<Tuple> zrangeByScoreWithScores(String key, double min,
//			double max, int offset, int count) {
//		return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
//	}
//
//	@Override
//	public Set<String> zrevrangeByScore(String key, String max, String min,
//			int offset, int count) {
//		return jedis.zrevrangeByScore(key, max, min, offset, count);
//	}
//
//	@Override
//	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
//		return jedis.zrangeByScoreWithScores(key, min, max);
//	}
//
//	@Override
//	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
//			String min) {
//		return jedis.zrevrangeByScoreWithScores(key, max, min);
//	}
//
//	@Override
//	public Set<Tuple> zrangeByScoreWithScores(String key, String min,
//			String max, int offset, int count) {
//		return jedis.zrangeByScoreWithScores(key, min, max, offset ,count);
//	}
//
//	@Override
//	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
//			double min, int offset, int count) {
//		return jedis.zrangeByScoreWithScores(key, max, min, offset, count);
//	}
//
//	@Override
//	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
//			String min, int offset, int count) {
//		return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
//	}
//
//	@Override
//	public Long zremrangeByRank(String key, long start, long end) {
//		return jedis.zremrangeByRank(key, start, end);
//	}
//
//	@Override
//	public Long zremrangeByScore(String key, double start, double end) {
//		return jedis.zremrangeByScore(key, start, end);
//	}
//
//	@Override
//	public Long zremrangeByScore(String key, String start, String end) {
//		return jedis.zremrangeByScore(key, start, end);
//	}
//
//	@Override
//	public Long linsert(String key, LIST_POSITION where, String pivot,
//			String value) {
//		return jedis.linsert(key, where, pivot, value);
//	}
//
//	@Override
//	public Long lpushx(String key, String... string) {
//		return jedis.lpushx(key, string);
//	}
//
//	@Override
//	public Long rpushx(String key, String... string) {
//		return jedis.rpushx(key, string);
//	}
//
//	@Override
//	public List<String> blpop(String arg) {
//		return jedis.blpop(arg);
//	}
//
//	@Override
//	public List<String> brpop(String arg) {
//		return jedis.brpop(arg);
//	}
//
//	@Override
//	public Long del(String key) {
//		return jedis.del(key);
//	}
//
//	@Override
//	public String echo(String string) {
//		return jedis.echo(string);
//	}
//
//	@Override
//	public Long move(String key, int dbIndex) {
//		return jedis.move(key, dbIndex);
//	}
//
//	@Override
//	public Long bitcount(String key) {
//		return jedis.bitcount(key);
//	}
//
//	@Override
//	public Long bitcount(String key, long start, long end) {
//		return jedis.bitcount(key, start, end);
//	}
//
//	@Override
//	@Deprecated
//	public
//	ScanResult<Entry<String, String>> hscan(String key, int cursor) {
////		return jedis.hscan(key, cursor);
//		Preconditions.checkNotNull(null);
//		return null;
//	}
//
//	@Override
//	@Deprecated
//	public
//	ScanResult<String> sscan(String key, int cursor) {
//		Preconditions.checkArgument(false);
//		return null;
//	}
//
//	@Override
//	@Deprecated
//	public
//	ScanResult<Tuple> zscan(String key, int cursor) {
//		Preconditions.checkArgument(false);
//		return null;
//	}
//
//	@Override
//	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
//		return jedis.hscan(key, cursor);
//	}
//
//	@Override
//	public ScanResult<String> sscan(String key, String cursor) {
//		return jedis.sscan(key, cursor);
//	}
//
//	@Override
//	public ScanResult<Tuple> zscan(String key, String cursor) {
//		return jedis.zscan(key, cursor);
//	}
//
//	@Override
//	public Long pfadd(String key, String... elements) {
//		return jedis.pfadd(key, elements);
//	}
//
//	@Override
//	public long pfcount(String key) {
//		return jedis.pfcount(key);
//	}
//
}
