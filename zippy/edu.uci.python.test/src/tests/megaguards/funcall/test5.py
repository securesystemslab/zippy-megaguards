a = [1, 2, 3]
c = [0, 0, 0]

def w(x, y):
    return x[y]*2

def t():
    for i in range(len(a)):
        c[i] = w(a,i)
t()
print(c)
