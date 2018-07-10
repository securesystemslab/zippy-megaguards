
def t():
    for j in range(len(a)):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2

a = [1, 2, 3]
b = [4, 5, 6]
c = [0, 0, 0]
t()

print(c)

a[0] = 10
a[1] = 20
a[2] = 30
b = [4, 5, 6]
c = [0, 0, 0]
t()

print(c)
