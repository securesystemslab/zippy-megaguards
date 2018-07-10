a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    a[x] = a[x] + x*2
    return a[x]*2

def t():
    for i in range(len(a)):
        c[i] = w(i)
t()
a[0] = 5
t()
print(c)
