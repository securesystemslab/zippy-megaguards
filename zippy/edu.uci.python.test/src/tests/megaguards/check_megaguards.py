# Copyright (c) 2018, Regents of the University of California
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


import sys, time, random

# N = int(sys.argv[1])
N = 2 ** 6

def mm_MG(X,Y,Z):
    # iterate through rows of X
    for i in range(len(X)):
       # iterate through columns of Y
       for j in range(len(Y[0])):
           # iterate through rows of Y
           for k in range(len(Y)):
               Z[i][j] += X[i][k] * Y[k][j]
    return Z


X = [[random.random() for i in range(N)] for j in range(N)]
Y = [[random.random() for i in range(N)] for j in range(N)]
Z = [[0.0 for i in range(N)] for j in range(N)]

print('Running matrix multiplication (%d x %d)..' % (N, N))
start = time.time()
mm_MG(X,Y,Z)
duration = "Matrix multiplication time: %.5f seconds" % ((time.time() - start))

print('Calculate maximum delta..')
delta = 0.0
for i in range(len(X)):
    # iterate through columns of Y
    for j in range(len(Y[0])):
        # iterate through rows of Y
        r = 0
        for k in range(len(Y)):
            r += X[i][k] * Y[k][j]

        delta = delta if abs(r - Z[i][j]) <= delta else abs(r - Z[i][j])

if delta == 0.0:
    print("Identical result compare to ZipPy")
else:
    print('maximum delta = %f' % delta)
print(duration)
# for r in result:
#    print(r)
