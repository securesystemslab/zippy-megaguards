from functools import reduce

def t(a, b):
    return a + b

print('%.0f' % reduce(t, [ i*.1+1 for i in range(10496000)]))
