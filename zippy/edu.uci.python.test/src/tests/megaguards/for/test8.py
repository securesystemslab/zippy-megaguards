a = [1, 2, 3]
c = [0, 0, 0]
def t():
    for i in range(len(a)):
        if( not a[i] == 2 ):
            c[i] = a[i]*2
t()
print(c)
