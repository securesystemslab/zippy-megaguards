import mx
import os, platform

_suite = mx.suite('zippy')
commit_hash = _suite.vc.parent(_suite.dir)

asv_env = os.environ.get("ZIPPY_ASV_PATH")
if not asv_env:
    asv_env = _suite.dir

asv_dir = asv_env + '/asv/'
asv_results_dir = asv_dir + '/results/'

machine_name = os.environ.get("MACHINE_NAME")
if not machine_name:
    machine_name = platform.node()

_mx_graal = mx.suite("compiler", fatalIfMissing=False)
machine_name += '-no-graal' if not _mx_graal else '-graal'

envoclcpu = 'CPU_PLATFORM_INDEX'
envoclgpu = 'GPU_PLATFORM_INDEX'

# testing
# machine_name = ''
# commit_hash  = ''

machine_results_dir = asv_results_dir + machine_name

chart_dir = asv_env + '/graphs/' + machine_name + '/'

yaxis_scale_range_list = [5, 10, 13, 32, 44, 65, 128, 160, 192, 256, 352, 512, 1024, 2048, 4096]
yaxis_scale_range = {
    '5'     : [i for i in range(6)],
    '10'    : [i for i in range(11)],
    '13'    : [i for i in range(14)],
    '32'    : [0, 1, 2, 4, 6, 8, 12, 16, 20, 24, 28, 32],
    '44'    : [0, 1, 2, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44],
    '65'    : [0, 1, 8, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65],
    '128'   : [0, 1, 2, 4, 8, 16, 28, 32, 48, 64, 92, 128],
    '160'   : [0, 1, 2, 4, 8, 16, 32, 64, 128, 160],
    '192'   : [0, 1, 2, 4, 8, 16, 32, 64, 128, 192],
    '256'   : [0, 1, 2, 4, 8, 16, 32, 64, 128, 256],
    '352'   : [0, 1, 4, 8, 16, 32, 64, 96, 128, 192, 256, 352],
    '512'   : [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512],
    '1024'  : [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024],
    '2048'  : [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048],
    '4096'  : [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096],
}


interpreter_list_cpu_cores_steps_ordered = [
    # ['ZipPy'        , 'ZipPy'                ],
    ['MG-CPU1'  , 'MegaGuards-CPU (1 CU)'  ],
    ['MG-CPU2'  , 'MegaGuards-CPU (2 CUs)' ],
    # ['MG-CPU3'  , 'MG-CPU (3 cores)' ],
    ['MG-CPU4'  , 'MegaGuards-CPU (4 CUs)' ],
    # ['MG-CPU5'  , 'MG-CPU (5 cores)' ],
    ['MG-CPU6'  , 'MegaGuards-CPU (6 CUs)' ],
    # ['MG-CPU7'  , 'MG-CPU (7 cores)' ],
    ['MG-CPU'   , 'MegaGuards-CPU (8 CUs)' ],
]

interpreter_list_steps_ordered = [
    # ['CPython'      , 'CPython3'        ],
    # ['PyPy3'        , 'PyPy3'           ],
    # ['ZipPy'        , 'ZipPy'           ],
    # ['MG-Truffle'       , 'MG-Truffle' ],
    ['MG-CPU'   , 'MegaGuards-CPU'      ],
    ['MG-GPU'   , 'MegaGuards-GPU'      ],
    ['MG'       , 'MegaGuards-Adaptive' ],
    ['OpenCL-GPU'   , 'OpenCL-GPU C/C++'],
    ['OpenCL-CPU'   , 'OpenCL-CPU C/C++']
]


interpreter_list_scales_ordered = [
    # ['ZipPy'        , 'ZipPy'           ],
    ['MG-CPU'   , 'MegaGuards-CPU'      ],
    ['MG-GPU'   , 'MegaGuards-GPU'      ],
    ['MG'       , 'MegaGuards-Adaptive' ],
    # ['OpenCL-GPU'   , 'OpenCL-GPU C/C++'],
    # ['OpenCL-CPU'   , 'OpenCL-CPU C/C++']
]

interpreter_list_ordered = [
    ['CPython'      , 'CPython3'        ],
    ['PyPy3'        , 'PyPy3'           ],
    ['ZipPy'        , 'ZipPy'           ],
    ['MG-CPU'   , 'MegaGuards-CPU'      ],
    # ['MG-NoDM'  , 'MG-NoDM'     ],
    ['MG-GPU'   , 'MegaGuards-GPU'      ],
    ['MG'       , 'MegaGuards-Adaptive' ],
    ['MG-Truffle'       , 'MegaGuards-Truffle' ],
    ['OpenCL-GPU'   , 'OpenCL-GPU C/C++'],
    ['OpenCL-CPU'   , 'OpenCL-CPU C/C++']
]

_Set1_colors = (
    (0.89411764705882357, 0.10196078431372549, 0.10980392156862745), # red
    (0.21568627450980393, 0.49411764705882355, 0.72156862745098038), # blue == red
    (0.30196078431372547, 0.68627450980392157, 0.29019607843137257), # green == gray == orange
    (0.59607843137254901, 0.30588235294117649, 0.63921568627450975), # purple == blue
    (1.0,                 0.49803921568627452, 0.0                ), # orange == green == gray
    (1.0,                 1.0,                 0.2                ), # yellow
    (0.65098039215686276, 0.33725490196078434, 0.15686274509803921), # brown
    (0.96862745098039216, 0.50588235294117645, 0.74901960784313726), # pink == orange
    (0.6,                 0.6,                 0.6),                 # gray == green == orange
    'k'                                                              # black
    )

_grayscale_colors = [
       'w'   ,
    '#3f3f3f',
    '#515151',
    '#999999',
    '#bababa',
    '#e2e2e2',
    '#777777',
    '#cccccc',
    '#444444',
       'k'   ,
    '#666666',
]

_color_set = _grayscale_colors

list_color_hatch_marker =  [
                        ['#999999',   '/',  '*-'],
                        ['#666666',   'o',  'r-'],
                        ['#515151',   's',  '^-'],
                        ['#444444',   '*',  '*-'],
                        ['#777777',    '',  'o-'],
                        ['#cccccc',    '',  'X-'],
                        ['#000000' ,   '',  's-'],
                    ]

interpreter_color_hatch_marker = {
    'OpenCL-GPU'   : [_color_set[0],   '',  '8-'],
    'OpenCL-CPU'   : [_color_set[1],  '',  'd-'],
    'MG-Truffle'   : [_color_set[6],  '',  'v-'],
    'MG-CPU'   : [_color_set[5],  '',  's-'],
    'MG-GPU'   : [_color_set[3],  '',  'o-'],
    'MG'       : [_color_set[9],   '',  'v-'],
    'PyPy3'        : [_color_set[7],   '',  '^-'],
    'CPython'      : [_color_set[8],   '',  'h-'],
    'ZipPy'        : [_color_set[9],   '',  '*-'],

    'MG-NoDM'  : [_color_set[10],   '',  'p-'],
    'MG-CPU1'  : [_color_set[8],   '',  'p-'],
    'MG-CPU2'  : [_color_set[1],  'o',  'd-'],
    'MG-CPU3'  : [_color_set[3],  '/',  'o-'],
    'MG-CPU4'  : [_color_set[6],   '',  '*-'],
    'MG-CPU5'  : [_color_set[9],   '',  '|-'],
    'MG-CPU6'  : [_color_set[7],   '',  'v-'],
    'MG-CPU7'  : [_color_set[0],   '',  'x-'],

    }

plot_scales_color_hatch_marker = {
    'OpenCL-GPU'   : [_color_set[8],   '',  'p-'],
    'OpenCL-CPU'   : [_color_set[1],  'o',  'd-'],
    'MG-CPU'   : [_color_set[2],  '.',  's-'],
    'MG-GPU'   : [_color_set[3],  '/',  'o-'],
    'MG'       : [_color_set[6],   '',  'v-'],
    'ZipPy'        : [_color_set[9],   '',  '*-'],
    }

plot_steps_color_hatch_marker = {
    'OpenCL-GPU'   : [_color_set[8],   '',  'p-'],
    'OpenCL-CPU'   : [_color_set[1],  'o',  'd-'],
    'MG-CPU'   : [_color_set[2],  '.',  's-'],
    'MG-GPU'   : [_color_set[3],  '/',  'o-'],
    'MG'       : [_color_set[6],   '',  '*-'],
    'ZipPy'        : [_color_set[9],   '',  '|-'],
    'PyPy3'        : [_color_set[7],   '',  'v-'],
    'CPython'      : [_color_set[10],   '',  'x-'],

    'MG-CPU1'  : [_color_set[8],   '',  'p-'],
    'MG-CPU2'  : [_color_set[1],  'o',  'd-'],
    'MG-CPU3'  : [_color_set[3],  '/',  'o-'],
    'MG-CPU4'  : [_color_set[6],   '',  '*-'],
    'MG-CPU5'  : [_color_set[9],   '',  '|-'],
    'MG-CPU6'  : [_color_set[7],   '',  'v-'],
    'MG-CPU7'  : [_color_set[10],   '',  'x-'],
    }

# ['k' ,        '' ,  's-'],
# ['#3f3f3f',   '*',  'o-'],
# ['#999999',   '' ,  '^-'],
# ['#bababa',   '/',  '*-'],
# ['#e2e2e2',   '.',  'p-'],
# ['#777777',    '',  'h-'],
# ['#cccccc',    '',  'v-'],
# ['w',         '' ,  '8-'],
# ['#666666',   'o',  'd-'],
# ['#515151',   's',  '^-'],
# ['#444444',   '*',  '*-'],
blacklist_bench_param = {
'nbody' : ['2048'],
'srad' : ['8192'],
'mm'    : ['64', '128', '256']
}

# blacklist_bench = ['hotspot3D', 'bfs', 'lavaMD', 'euler3d', 'backprop']
blacklist_bench = ['hotspot3D', 'backprop']
