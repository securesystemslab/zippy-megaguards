from functools import reduce

def t(a, b):
    return a + b + 3

print(reduce(t, [ i+1 for i in range(2000)]))
