a = [ i for i in range(10000)]
b = [ i*2 for i in range(10000)]
c = [ 0 for i in range(10000)]
def t():
    for j in range(1,len(a)-1):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2
t()
print(c)
