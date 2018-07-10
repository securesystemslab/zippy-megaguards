a = [ i for i in range(100)]
b = [ i*2 for i in range(100)]
c = [ 0 for i in range(100)]
def t():
    for j in range(1,len(a)-1, 2):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2
        if j > 80:
            break
t()
print(c)
