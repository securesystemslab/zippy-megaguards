a = [1, 2, 3]
b = [4, 5, 6]
c = [0, 0, 0]

def w(x, y):
    z = x + y*2
    return z

def t():
    for j in range(len(a)):
        for i in range(len(b)):
            c[j] = w(b[i], a[j])
t()
print(c)
