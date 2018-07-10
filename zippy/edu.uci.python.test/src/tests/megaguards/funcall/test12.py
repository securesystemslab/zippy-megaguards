a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    return a[x]*2 if x == 0 else a[x]*3

def t():
    for i in range(len(a)):
        c[i] = w(i)
t()
print(c)
