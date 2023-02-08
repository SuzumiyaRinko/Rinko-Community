redis.call("multi")

redis.call('set', 'name', 'juejue')
redis.call('incr', 'name')

redis.call("commit")