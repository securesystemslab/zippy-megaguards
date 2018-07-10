a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    return x

def t():
    for i in range(len(a)):
        c[i] = w(a[i]*2)
t()
print(c)
