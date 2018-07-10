N=3

def t(x):
    a = [[ i+1 for i in range(N)] for j in range(N) ]
    for i in range(3):
        for j in range(3):
            for k in range(x):
                c[0][k] = a[0][k]*2


c = [[ 0 for i in range(N)] for j in range(N) ]
t(2)
c = [[ 0 for i in range(N)] for j in range(N) ]
t(3)
c = [[ 0 for i in range(N)] for j in range(N) ]
t(2)
print(c[0])
