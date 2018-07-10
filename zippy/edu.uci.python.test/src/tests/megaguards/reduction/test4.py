from functools import reduce

print(reduce(lambda a, b: a + b, [ i+1 for i in range(2000)]))
