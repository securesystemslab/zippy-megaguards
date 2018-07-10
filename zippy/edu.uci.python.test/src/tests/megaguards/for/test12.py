a = [1, 2, 3]
c = [0, 0, 0]
def t(l):
    for i in range(l):
        c[i] = a[i] * 2 if a[i] == 2 else a[i] * 3
        x = a[i] * 2 if a[i] == 2 else a[i] * 3
        c[i] = x
t(2)
print(c)
t(3)
print(c)
