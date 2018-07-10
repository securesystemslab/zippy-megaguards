a = [1, 2, 3]
c = [0, 0, 0]
def t():
    for i in range(len(a)):
        x = a[i]*2
        c[i] = x
    y = min(x + 1, 0)
    print(y)
t()
print(c)
