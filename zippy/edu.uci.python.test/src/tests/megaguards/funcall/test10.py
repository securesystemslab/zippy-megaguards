a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    if x > 2:
        return a[x]*2
    y = a[x]*3
    if x > 1:
        return y

def t():
    for i in range(len(a)):
        c[i] = w(i)
t()
print(c)
