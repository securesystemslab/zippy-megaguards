a = [ i for i in range(100)]
b = [ i*2 for i in range(100)]
c = [ 0 for i in range(100)]

def w(d, e, f):
    for i in range(100):
        d[i] += b[i] + e[i]*2

def t():
    for j in range(1,len(a)-1, 2):
        w(c,a,b)
        if j > 20:
            break
t()
print(c)
