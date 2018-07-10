a = [1.3, 1.6, 2.5]
c = [0., 0., 0.]
def t():
    for i in range(len(a)):
        c[i] = round(a[i])*2
t()
print(c)
