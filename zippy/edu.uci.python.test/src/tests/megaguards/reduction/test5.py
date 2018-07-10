from functools import reduce

def t(a, b):
    return a + b

print(reduce(t, [ i+1 for i in range(4096)]))
