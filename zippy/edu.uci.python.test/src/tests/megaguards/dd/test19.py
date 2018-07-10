a = [1., 2., 3.]
c = [0., 0., 0.]

def n(y, j):
    y[j] = y[j] * 2
    return y[j]


def m(y, j):
    u = n(y, j);
    v = n(y, j);
    return u + v;

def w(x):
    return m(a, 0)

def t():
    for i in range(len(a)):
        c[i] = w(i)

def tt():
    for i in range(len(a)):
        a[0] = a[0] * 2
        u = a[0]
        a[0] = a[0] * 2
        v = a[0]
        c[i] = u + v

# tt()
t()
a[0] = 5.
t()
print(c)
