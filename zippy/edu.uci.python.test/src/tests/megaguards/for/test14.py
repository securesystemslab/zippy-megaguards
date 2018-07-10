N=3

def t():
    a = [[ i+1 for i in range(N)] for j in range(N) ]
    for i in range(N):
        for j in range(N):
            for k in range(N):
                c[i][j] = a[i][j]*2


c = [[ 0 for i in range(N)] for j in range(N) ]
t()
c = [[ 0 for i in range(N)] for j in range(N) ]
t()
c = [[ 0 for i in range(N)] for j in range(N) ]
t()
print(c[0])
