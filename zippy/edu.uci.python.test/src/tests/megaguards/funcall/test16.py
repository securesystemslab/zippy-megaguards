a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    a[x] = a[x]*2

def t():
    for i in range(len(a)):
        w(i)
        c[i] = a[i]
t()
print(c)
