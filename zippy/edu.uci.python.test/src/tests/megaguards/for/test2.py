a = [2 * n for n in range(50)]
c = [0 for i in range(50)]
def t():
    for i in range(len(a)):
        c[i] = a[i]*2
t()
print(c)
