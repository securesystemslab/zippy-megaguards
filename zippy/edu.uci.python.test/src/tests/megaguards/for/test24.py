
def t(x):
    b = x
    for j in range(len(a)):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2

a = [1, 2, 3]
d = [4, 5, 6]
c = [0, 0, 0]
t(d)

print(c)

a = [10, 20, 30]
d = [4.2, 5.3, 6.4]
c = [0., 0., 0.]
t(d)

print(c)
