a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    a[0] = a[0] + x*2
    return a[0]*2

def t():
    for i in range(len(a)):
        c[i] = w(i)
t()
a[0] = 5
t()
print(c)
