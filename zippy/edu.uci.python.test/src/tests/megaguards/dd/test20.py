b = [1, 2, 3]
c = [0, 0, 0]

def w(a, i):
    a[i] = a[i] + i*2
    return a[i]*2

def t():
    for i in range(len(b)):
        c[i] = w(b,0)
t()
b[0] = 5
t()
print(c)
