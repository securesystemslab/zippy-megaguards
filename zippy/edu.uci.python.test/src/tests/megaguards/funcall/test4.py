a = [1, 2, 3]
c = [0, 0, 0]

def m():
    return 2

def w(x):
    y = -x*m()
    return y

def t():
    for i in range(len(a)):
        x = w(a[i])
        c[i] = x
t()
print(c)
