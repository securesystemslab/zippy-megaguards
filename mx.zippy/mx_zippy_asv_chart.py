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

from mx_mg_conf import *
from mx_mg_pages import *

from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
import os
from os.path import join, exists
import platform
import json
import mx
import math
import re
import datetime
import copy
from mx_zippy_bench_param import benchmarks_list


importerror = False
try:
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    import matplotlib.ticker as ticker
    from scipy import stats
    import numpy as np
    from functools import reduce
except:
    # raise
    importerror = True

debug = False

if not importerror:
    geomean = lambda s: reduce(lambda x,y: x*y, s) ** (1.0 / len(s))


signed_date                 = ''
interpreters_versions       = {}
benchmarks_images           = {}
benchmarks_stats_each       = {}
benchmarks_stats_types      = {}
benchmarks_stats_overall    = {}


def add_steps_geomean(_type, benchmarks, all_benchmarks_list):
    r_benchmarks = {}
    for _interp in benchmarks:
        if _interp not in r_benchmarks:
            r_benchmarks[_interp] = []
        for _bench in sorted(all_benchmarks_list):
            # print('type: '+ _type + '  bench: ' + _bench)
            _largest_parm = benchmarks_list[_type][1][_bench][-2]
            _time_steps = benchmarks[_interp][_bench][_largest_parm][0]
            _step_count = len(_time_steps)
            if len(r_benchmarks[_interp]) == 0:
                r_benchmarks[_interp] = [[] for i in range(_step_count)]
            for _step in range(_step_count):
                r_benchmarks[_interp][_step] += [_time_steps[_step]]

        # geomean
        geomean_str = 'GeoMean'
        r_benchmarks[_interp] = [geomean(r_benchmarks[_interp][_step]) for _step in range(len(r_benchmarks[_interp]))]
        benchmarks[_interp][geomean_str] = { geomean_str : [None] }
        benchmarks[_interp][geomean_str][geomean_str][0] = r_benchmarks[_interp]
    all_benchmarks_list[geomean_str] = [geomean_str]

def custom_normalization_step(_type, benchmarks, interpreter_list):
    # XXX: err_bar hasn't been normalized
    _timing = "Steps"
    r_benchmarks_normalized = copy.deepcopy(benchmarks[_type][_timing])
    r_param_max = {}
    r_param_yaxis = {}
    # p = 5
    for _interp_w_name in range(len(interpreter_list)):
        _interp = interpreter_list[_interp_w_name][0]
        if _interp not in r_benchmarks_normalized:
            continue

        for _bench in r_benchmarks_normalized[_interp]:
            if _bench not in r_param_max:
                r_param_max[_bench] = {}
            for _param in r_benchmarks_normalized[_interp][_bench]:
                if _param not in r_param_max[_bench]:
                    r_param_max[_bench][_param] = 0.
                r_param_max[_bench][_param] = max(r_param_max[_bench][_param], max(r_benchmarks_normalized[_interp][_bench][_param][0]))


    for _bench in r_param_max:
        if _bench not in r_param_yaxis:
            r_param_yaxis[_bench] = {}

        for _param in r_param_max[_bench]:
            m = r_param_max[_bench][_param]
            r = yaxis_scale_range['5']
            for i in range(len(yaxis_scale_range_list)):
                r = yaxis_scale_range['%s' % yaxis_scale_range_list[i]]
                if yaxis_scale_range_list[i] > m:
                    break
            r_param_yaxis[_bench][_param] = r

    for _interp in r_benchmarks_normalized:
        for _bench in r_benchmarks_normalized[_interp]:
            for _param in r_benchmarks_normalized[_interp][_bench]:
                yaxis_local_range = r_param_yaxis[_bench][_param]
                yaxis_range_len = len(yaxis_local_range) - 1
                for _step in range(len(r_benchmarks_normalized[_interp][_bench][_param][0])):
                    _time = r_benchmarks_normalized[_interp][_bench][_param][0][_step]
                    n = _time
                    for j in range(yaxis_range_len):
                        n = (_time - yaxis_local_range[j])/(yaxis_local_range[j + 1] - yaxis_local_range[j])
                        if n > 1 and j < (yaxis_range_len - 1):
                            continue
                        n = n + j
                        break
                    r_benchmarks_normalized[_interp][_bench][_param][0][_step] = n

    return r_benchmarks_normalized, r_param_yaxis


def plot_steps(benchmarks_origin, all_benchmarks_list_origin, base, interpreter_list, step_str='steps', single_benchmark=None, filename_prefix=''):
    benchmarks = copy.deepcopy(benchmarks_origin)
    all_benchmarks_list = copy.deepcopy(all_benchmarks_list_origin)

    legend_num_col = len(interpreter_list)
    legend_num_col = legend_num_col / 2 + legend_num_col % 2
    for _type in benchmarks:
        _timing = 'Steps'
        add_steps_geomean(_type, benchmarks[_type][_timing], all_benchmarks_list[_type][_timing])
        r_benchmarks_normalized, r_param_yaxis = custom_normalization_step(_type, benchmarks, interpreter_list)
        for _bench in all_benchmarks_list[_type][_timing]:
            print("%s %s" % (single_benchmark, _bench))
            if single_benchmark and _bench != single_benchmark:
                continue

            _param_axs = {}
            _param_xticks = {}
            for _param in all_benchmarks_list[_type][_timing][_bench]:
                _param_axs[_param] = None
            if not _param_axs:
                continue
            subplots_count = len(_param_axs)
            axs = []
            max_col_subplot = 3
            if subplots_count == 1:
                fig, axs2d = plt.subplots(1, 1, figsize=(8, 4))#, figsize=(2.5 * int(subplots_count / 2), 5))
                axs = [axs2d]
            elif subplots_count <= max_col_subplot:
                # print(subplots_count)
                fig, axs2d = plt.subplots(1, subplots_count)#, figsize=(2.5 * int(subplots_count / 2), 5))
                for i in range(len(axs2d)):
                    axs2d[i].axis('off')
                    axs += [axs2d[i]]
            else:
                row_count = float(subplots_count)/max_col_subplot
                row_count = int(row_count) if (int(row_count) - row_count) == 0. else (int(row_count) + 1)
                # print("%d: %d x %d" % (subplots_count, row_count, col_count))
                fig, axs2d = plt.subplots(row_count, max_col_subplot)#, figsize=(2.5 * int(subplots_count / 2), 5))
                # print(axs2d)
                for i in range(len(axs2d)):
                    for j in range(len(axs2d[i])):
                        axs2d[i][j].axis('off')
                        axs += [axs2d[i][j]]

            fig.subplots_adjust(left=0.06, right=0.98, wspace=0.01, hspace=0.02)

            axs_i = 0
            for _param in sorted(_param_axs.keys()):
                axs[axs_i].axis('on')
                _param_axs[_param] = axs[axs_i]
                # axs[axs_i].set_autoscalex_on(False)
                axs_i += 1

            # for _interp in benchmarks[_type][_timing]:
            lines = [[], []]
            for _interp_w_name in range(len(interpreter_list)):
                _interp = interpreter_list[_interp_w_name][0]
                if _interp not in benchmarks[_type][_timing]:
                    continue

                for _param in benchmarks[_type][_timing][_interp][_bench]:
                    ax = _param_axs[_param]
                    _param_xticks[_param] = [ i+1 for i in range(len(benchmarks[_type][_timing][_interp][_bench][_param][0]))]
                    marker = plot_steps_color_hatch_marker[_interp][2]
                    color  = plot_steps_color_hatch_marker[_interp][0]
                    # line = ax.plot(_params, r_bench_params, marker, color=color) # label=_interp
                    line, = ax.plot(range(len(_param_xticks[_param])), r_benchmarks_normalized[_interp][_bench][_param][0], marker, color=color) # label=_interp
                lines[0] += [line]
                lines[1] += [interpreter_list[_interp_w_name][1]]

            for _param in _param_xticks:
                _params_str = _param_xticks[_param]
                _params_int = []
                for i in range(len(_params_str)):
                    _params_int += [int(_params_str[i])]
                _param_xticks[_param] = [_params_int, _params_str]

            for _param in sorted(_param_axs.keys()):
                ax = _param_axs[_param]
                # ax.set_xscale('log')
                _params_len = len(_param_xticks[_param][0])
                ax.set_xticks(range(_params_len))
                # ax.set_ylim(-0.5, ax.get_ylim()[1] + 0.5)
                ax.set_xlim(-0.5, _params_len - 0.5)
                ax.set_xticklabels(_param_xticks[_param][1], fontsize='large') # , rotation=45
                # ax.legend(loc='upper left')
                # ax.set_yscale('linear') # 'log'
                ax.set_yticks(range(len(r_param_yaxis[_bench][_param])))
                ax.set_yticklabels(['%s' % x for x in r_param_yaxis[_bench][_param]],fontsize='x-small')
                ax.set_title(_param, fontsize='medium')
                ax.grid(True)

            fig.legend(lines[0], lines[1], fontsize='small', ncol=legend_num_col,
                      bbox_to_anchor=(0., 1.17, 1., .0),
                      loc=9
                    #   mode="expand"
                      )
            fig.text(0.5, -0.02, 'Running steps', fontsize='large', ha='center')
            fig.text(0.00, 0.5, "Speedup over " + base, fontsize='large', va='center', rotation='vertical')

            # fig.ylabel("Speedup over " + base, fontsize='large')
            filename = filename_prefix+'benchmarks_' + step_str + '_' + _bench
            # benchmarks_images['Steps `' + _type + '` benchmarks chart measuring `' + _timing + '` timing:'] = filename
            benchmarks_images[filename_prefix + ' Steps'] = filename
            fig.savefig(chart_dir + filename + '.png', bbox_inches='tight')
            fig.savefig(chart_dir + filename + '.pdf', format='pdf', bbox_inches='tight')
            plt.close(fig)


def pre_plot_scales_collect_params(_type, _timing, all_benchmarks_list, benchmarks):
    r_bench_params = {}
    r_bench_max = {}
    # p = 5
    for _interp_w_name in range(len(interpreter_list_scales_ordered)):
        _interp = interpreter_list_scales_ordered[_interp_w_name][0]
        r_bench_params[_interp] = {}
        for _bench in benchmarks[_interp]:
            if _bench not in r_bench_max:
                r_bench_max[_bench] = 0.

            r_bench_params[_interp][_bench] = []
            _params = all_benchmarks_list[_type][_timing][_bench]
            for _param in _params:
                r_bench_params[_interp][_bench] += [benchmarks[_interp][_bench][_param][0]]
                # if p > 0:
                #     p -= 1
                #     print(benchmarks[_interp][_bench][_param])

            r_bench_max[_bench] = max(r_bench_max[_bench], max(r_bench_params[_interp][_bench]))
    # print(r_bench_max)
    return r_bench_params, r_bench_max


def custom_normalization_scale(r_benchmarks, r_bench_max):
    r_benchmarks_normalized = copy.deepcopy(r_benchmarks)
    r_bench_yaxis = {}
    for _bench in r_bench_max:
        m = r_bench_max[_bench]
        r = yaxis_scale_range['5']
        for i in range(len(yaxis_scale_range_list)):
            r = yaxis_scale_range['%s' % yaxis_scale_range_list[i]]
            if yaxis_scale_range_list[i] > m:
                break
        r_bench_yaxis[_bench] = r

    for _interp in r_benchmarks_normalized:
        for _bench in r_benchmarks_normalized[_interp]:
            yaxis_local_range = r_bench_yaxis[_bench]
            yaxis_range_len = len(yaxis_local_range) - 1
            for _param in range(len(r_benchmarks_normalized[_interp][_bench])):
                _time = r_benchmarks_normalized[_interp][_bench][_param]
                n = _time
                for j in range(yaxis_range_len):
                    n = (_time - yaxis_local_range[j])/(yaxis_local_range[j + 1] - yaxis_local_range[j])
                    if n > 1 and j < (yaxis_range_len - 1):
                        continue
                    n = n + j
                    break
                r_benchmarks_normalized[_interp][_bench][_param] = n

    return r_benchmarks_normalized, r_bench_yaxis


def plot_scales(benchmarks, all_benchmarks_list, base, filename_prefix=''):

    for _type in benchmarks:
        for _timing in benchmarks[_type]:
            # if _timing == 'Time':
            #     continue
            if _timing == 'Steps':
                continue
            if _timing == 'Warmup':
                continue
            _bench_axs = {}
            _bench_xticks = {}
            for _bench in all_benchmarks_list[_type][_timing]:
                _bench_axs[_bench] = None
            if not _bench_axs:
                continue
            subplots_count = len(_bench_axs)
            # print(subplots_count)
            fig, axs2d = plt.subplots(2, int(subplots_count / 2), figsize=(2.5 * int(subplots_count / 2), 4))
            fig.subplots_adjust(left=0.08, right=0.98, wspace=0.3, hspace=0.5)
            axs = []
            for i in range(len(axs2d)):
                for j in range(len(axs2d[i])):
                    axs += [axs2d[i][j]]

            axs_i = 0
            for _bench in sorted(_bench_axs.keys()):
                _bench_axs[_bench] = axs[axs_i]
                # axs[axs_i].set_autoscalex_on(False)
                axs_i += 1

            r_benchmarks, r_bench_max = pre_plot_scales_collect_params(_type, _timing, all_benchmarks_list, benchmarks[_type][_timing])
            r_benchmarks_normalized, r_bench_yaxis = custom_normalization_scale(r_benchmarks, r_bench_max)

            # for _interp in benchmarks[_type][_timing]:
            lines = [[], []]
            for _interp_w_name in range(len(interpreter_list_scales_ordered)):
                _interp = interpreter_list_scales_ordered[_interp_w_name][0]
                for _bench in benchmarks[_type][_timing][_interp]:
                    # if _bench not in _bench_xticks:
                    #     _bench_xticks[_bench] = []
                    ax = _bench_axs[_bench]
                    _params = all_benchmarks_list[_type][_timing][_bench]
                    # print(_params)
                    _bench_xticks[_bench] = _params
                    # r_bench_params = pre_plot_scales_collect_params(_params, benchmarks[_type][_timing][_interp][_bench])
                    marker = plot_scales_color_hatch_marker[_interp][2]
                    color  = plot_scales_color_hatch_marker[_interp][0]
                    # line = ax.plot(_params, r_bench_params, marker, color=color) # label=_interp
                    line, = ax.plot(range(len(_params)), r_benchmarks_normalized[_interp][_bench], marker, color=color) # label=_interp
                lines[0] += [line]
                lines[1] += [interpreter_list_scales_ordered[_interp_w_name][1]]

            for _bench in _bench_xticks:
                _params_str = _bench_xticks[_bench]
                _params_int = []
                for _param in range(len(_params_str)):
                    _params_int += [_params_str[_param]]
                _bench_xticks[_bench] = [_params_int, _params_str]

            for _bench in sorted(_bench_axs.keys()):
                ax = _bench_axs[_bench]
                # ax.set_xscale('log')
                _params_len = len(_bench_xticks[_bench][0])
                ax.set_xticks(range(_params_len))
                # ax.set_ylim(-0.5, ax.get_ylim()[1] + 0.5)
                ax.set_xlim(-0.5, _params_len - 0.5)
                ax.set_xticklabels(_bench_xticks[_bench][1], rotation=45, fontsize='xx-small')
                # ax.legend(loc='upper left')
                # ax.set_yscale('linear') # 'log'
                ax.set_yticks(range(len(r_bench_yaxis[_bench])))
                yticklabels_str = ['%s' % x for x in r_bench_yaxis[_bench]]

                ax.set_yticklabels(yticklabels_str,fontsize='xx-small')
                ax.set_title(_bench, fontsize='medium')
                ax.grid(True)

            fig.legend(lines[0], lines[1], fontsize='medium', ncol=6,
                      bbox_to_anchor=(0., 1.15, .98, .0),
                      loc=9
                    #   mode="expand"
                      )
            fig.text(0.5, -0.05, 'Benchmarks input sizes', ha='center')
            fig.text(0.04, 0.5, "Speedup over " + base, fontsize='large', va='center', rotation='vertical')

            # fig.ylabel("Speedup over " + base, fontsize='large')
            filename = filename_prefix+'benchmarks_scales_' + _type + '_' + _timing
            # benchmarks_images['Scale `' + _type + '` benchmarks chart measuring `' + _timing + '` timing:'] = filename
            benchmarks_images[filename_prefix + ' Scale'] = filename
            fig.savefig(chart_dir + filename + '.png', bbox_inches='tight')
            fig.savefig(chart_dir + filename + '.pdf', format='pdf', bbox_inches='tight')
            plt.close(fig)


def plot_bar_speedups_dm(r_benchmarks_normalized, r_benchmarks_ci_normalized, benchmarks, all_benchmarks_list, _type, _timing, small=True, filename_prefix=''):
    size = len(all_benchmarks_list)
    size = (12,  4)
    fig = plt.figure(figsize=size) #, dpi=80)
    ax = fig.add_subplot(1, 1, 1)

    ax.xaxis.tick_top()
    ax.tick_params(labeltop='off')
    ax.xaxis.tick_bottom()
    ax.spines['top'].set_visible(False)
    ly = len(all_benchmarks_list)
    xticks = np.arange(1, ly + 1)
    r_benchmarks_dm = []
    r_benchmarks_dm_ci = []
    for i in range(len(benchmarks['MG-GPU'])):
        r_benchmarks_dm += [benchmarks['MG-GPU'][i] / benchmarks['MG-NoDM'][i]]

    r = ax.bar( xticks, r_benchmarks_dm,  .5, align='center',
                color='k')

    ax.set_xticks(xticks)

    if small:
        ax.set_xticklabels(all_benchmarks_list, fontsize='small')#, rotation=45, horizontalalignment='right')
    else:
        ax.set_xticklabels(all_benchmarks_list, fontsize=17)#, rotation=45)

    (y_bottom, y_top) = ax.get_ylim()
    y_height = y_top - y_bottom
    y_height = np.log2(y_height)

    def autolabel(rects, y_height):
        i = 0
        for rect in rects:
            height = rect.get_height()
            p_height = np.log2(height) / (y_height)
            max_hight = 0.90 if small else 0.90
            label_rotation ='horizontal'

            if small:
                fontsize='x-small'
            else:
                fontsize='large'

            ax.text( rect.get_x() + rect.get_width()/2.,
                     1.02*height,
                     '%.2f' % r_benchmarks_dm[i],
                     ha='center',
                     va='bottom',
                     fontsize=fontsize, # fontsize='medium'
                     fontweight='bold',
                     rotation=label_rotation)
            i += 1
    autolabel(r, y_height)

    ax.set_xlim(.3, ly + .7)
    ax.yaxis.grid(True)
    ax.set_ylabel("Speedup over MegaGuards-GPU\nwithout KDM", fontsize='medium')
    ax.set_yscale('symlog', basey=2)
    ax.tick_params(direction='out')
    # ax.set_yticks([0, 1, 2, 4, 8, 16, 32])
    # ax.set_yticklabels([0, 1, 2, 4, 8, 16, 32, 64],fontsize=11)

    fig.subplots_adjust(left=0.05)
    filename = filename_prefix+'benchmarks_bar_KDM_' + _type + '_' + _timing
    benchmarks_images[filename_prefix + ' KDM Optimization'] = filename
    fig.savefig(chart_dir + filename + '.png', bbox_inches='tight')
    fig.savefig(chart_dir + filename + '.pdf', format='pdf', bbox_inches='tight')
    plt.close(fig)


def plot_bar_speedups(ax, r_benchmarks_normalized, r_benchmarks_ci_normalized, benchmarks, all_benchmarks_list, interpreter_list, color_hatch, witdh, small=True):
    ax.xaxis.tick_top()
    ax.tick_params(labeltop='off')
    ax.xaxis.tick_bottom()
    ax.spines['top'].set_visible(False)
    ly = len(all_benchmarks_list)
    xticks = np.arange(1, ly + 1)
    # ax.bar(xticks, y, align='center')
    c_witdh = 0
    rects = [[], [], []]
    interpreter_list_ordered_filtered = []
    for _interp in interpreter_list_ordered:
        if _interp[0] in interpreter_list:
            interpreter_list_ordered_filtered += [_interp]

    for i in range(len(interpreter_list_ordered_filtered)):
        s = interpreter_list_ordered_filtered[i][0]

        r = ax.bar( xticks + c_witdh, r_benchmarks_normalized[s],  witdh, align='center',
                    yerr=r_benchmarks_ci_normalized[s], capsize=1, ecolor='black',
                    color=color_hatch[s][0], hatch=color_hatch[s][1])
        rects[0] += [r]
        rects[1] += [s]
        rects[2] += [interpreter_list_ordered_filtered[i][1]]
        c_witdh += witdh

    ax.set_xticks(xticks + c_witdh/2.3)

    if small:
        ax.set_xticklabels(all_benchmarks_list, fontsize=17)#, rotation=45)
    else:
        ax.set_xticklabels(all_benchmarks_list, fontsize=17)#, rotation=45)

    (y_bottom, y_top) = ax.get_ylim()
    y_height = y_top - y_bottom
    y_height = np.log2(y_height)

    def autolabel(rects, s, y_height):
        i = 0
        for rect in rects:
            height = rect.get_height()
            p_height = np.log2(height) / (y_height)
            max_hight = 0.90 if small else 0.90

            label_rotation ='vertical'

            if small:
                fontsize='small'
            else:
                fontsize='large'

            ax.text( rect.get_x() + rect.get_width()/2.,
                     1.02*height,
                     '%.2f' % benchmarks[s][i],
                     ha='center',
                     va='bottom',
                     fontsize=fontsize, # fontsize='medium'
                     fontweight='bold',
                     rotation=label_rotation)
            i += 1
    for r, s in zip(rects[0], rects[1]):
        autolabel(r, s, y_height)

    if small:
        ax.legend(rects[0], rects[2], fontsize='large', ncol=5,
                  bbox_to_anchor=(0., 1.12, 1., .102), loc=3, mode="expand")
    else:
        ax.legend(rects[0], rects[2], fontsize='large', ncol=5, mode="expand")

    ax.set_xlim(.7, ly + 1)
    ax.yaxis.grid(True)

def custom_normalization(r_benchmarks, yaxis_range):
    r_benchmarks_normalized = copy.deepcopy(r_benchmarks)
    yaxis_range_len = len(yaxis_range) - 1
    for _interp in r_benchmarks_normalized:
        for i in range(len(r_benchmarks_normalized[_interp])):
            _time = r_benchmarks_normalized[_interp][i]
            n = _time
            for j in range(yaxis_range_len):
                n = (_time - yaxis_range[j])/(yaxis_range[j + 1] - yaxis_range[j])
                if n > 1 and j < (yaxis_range_len - 1):
                    continue
                n = n + j
                break
            r_benchmarks_normalized[_interp][i] = n
    return r_benchmarks_normalized


def custom_normalization_ci(r_benchmarks_ci, r_benchmarks_normalized, r_benchmarks, yaxis_range):
    r_benchmarks_ci_normalized = copy.deepcopy(r_benchmarks_ci)
    yaxis_range_len = len(yaxis_range) - 1
    for _interp in r_benchmarks_ci_normalized:
        for i in range(len(r_benchmarks_ci_normalized[_interp])):
            _ci = r_benchmarks_ci_normalized[_interp][i]
            _time = r_benchmarks[_interp][i]
            n = _ci
            for j in range(yaxis_range_len):
                n = ((_ci + _time) - yaxis_range[j])/(yaxis_range[j + 1] - yaxis_range[j])
                if n > 1 and j < (yaxis_range_len - 1):
                    continue
                n = n + j
                break
            r_benchmarks_ci_normalized[_interp][i] = n - r_benchmarks_normalized[_interp][i]
    return r_benchmarks_ci_normalized

def pre_process_plot(benchmarks, all_benchmarks_list, _type, use_largest=True):
    r_benchmarks = {}
    r_benchmarks_ci = {}
    r_benchmarks_list = {}
    is_bench_list_complete = False
    for _interp in benchmarks:
        if _interp not in r_benchmarks:
            r_benchmarks[_interp] = []
            r_benchmarks_ci[_interp] = []
            r_benchmarks_list = []
        for _bench in sorted(all_benchmarks_list):
            c_bench_list = []
            idx_bench = int( len(all_benchmarks_list[_bench]) / 2 )
            _bench_params = all_benchmarks_list[_bench]

            if not use_largest:
                for idx in range(len(_bench_params)):
                    _param = _bench_params[idx]
                    r_benchmarks[_interp] += [benchmarks[_interp][_bench][_param][0]]
                    r_benchmarks_ci[_interp] += [benchmarks[_interp][_bench][_param][1]]

                    c_bench_list += [(_param + '\n' + _bench)]

            else:
                _largest_parm = benchmarks_list[_type][1][_bench][-2]
                r_benchmarks[_interp] += [benchmarks[_interp][_bench][_largest_parm][0]]
                r_benchmarks_ci[_interp] += [benchmarks[_interp][_bench][_largest_parm][1]]
                c_bench_list += [_bench]
            if not is_bench_list_complete:
                r_benchmarks_list += c_bench_list

        # geomean
        r_benchmarks[_interp] += [geomean(r_benchmarks[_interp])]
        r_benchmarks_ci[_interp] += [geomean(r_benchmarks_ci[_interp])]
        print('%s  geomean: %f (%.3f)' % (_interp, r_benchmarks[_interp][-1], r_benchmarks_ci[_interp][-1]))
    r_benchmarks_list += ['GeoMean']
    r_benchmarks_normalized = r_benchmarks#custom_normalization(r_benchmarks, yaxis_range)
    r_benchmarks_ci_normalized = r_benchmarks_ci#custom_normalization_ci(r_benchmarks_ci, r_benchmarks_normalized, r_benchmarks, yaxis_range)
    return r_benchmarks_normalized, r_benchmarks_ci_normalized, r_benchmarks, r_benchmarks_list

# yaxis_range = [0.001, .1, 1, 2, 5, 10, 20, 100, 200, 300, 400]
yaxis_range_001_5 = [0.001, .1, 1, 2, 5]
yaxis_range_10_100 = [i for i in range(10, 100, 10)]
yaxis_range_100_350 = [i for i in range(100, 330, 10)]
yaxis_range = yaxis_range_001_5 + yaxis_range_10_100 + yaxis_range_100_350

yaxis_range_str = [ '%s' % i for i in yaxis_range_001_5] + [ '%s' % i for i in yaxis_range_10_100] + [ '%s' % i if (i % 50) == 0 else '' for i in yaxis_range_100_350]
# yaxis_range_str = [0.001, .1, 1, 2, 5] + [i for i in range(10, 400, 10)]

def process_plot(benchmarks, all_benchmarks_list, interpreter_list, color_hatch_marker, base, only_dm=False, filename_prefix=''):
    for _type in benchmarks:
        for _timing in benchmarks[_type]:
            if _timing == 'Steps':
                continue
            if _timing == 'Warmup':
                continue
            r_benchmarks_normalized, r_benchmarks_ci_normalized, r_benchmarks, r_bench_list = pre_process_plot(benchmarks[_type][_timing], all_benchmarks_list[_type][_timing], _type)
            # markdown_overall_speedups(_type, _timing, r_benchmarks, benchmarks_stats_types, benchmarks_stats_overall)
            size = len(r_bench_list)
            size = (max(size * 2, 8), min(size, 7))
            fig = plt.figure(figsize=size) #, dpi=80)
            ax = fig.add_subplot(1, 1, 1)
            # interpreter_list.remove(base)
            # r_benchmarks_normalized.pop(base)
            width = 1. / (len(interpreter_list) + 2) # +1 for spacing and -1 for base
            plot_bar_speedups(ax, r_benchmarks_normalized, r_benchmarks_ci_normalized, r_benchmarks, r_bench_list, interpreter_list, color_hatch_marker, width)
            if 'MG-GPU' in interpreter_list and 'MG-NoDM' in interpreter_list and only_dm:
                plot_bar_speedups_dm(r_benchmarks_normalized, r_benchmarks_ci_normalized, r_benchmarks, r_bench_list, _type, _timing, filename_prefix=filename_prefix)
                continue
            # ax.set_xlabel("Benchmarks (" + _type + ") (normalized to " + base + ")")
            ax.set_ylabel("Speedup over " + base, fontsize='large')
            ax.set_yscale('symlog', basey=2)
            ax.tick_params(direction='out')
            # ax.set_yticks(range(len(yaxis_range)))
            # ax.set_yticks([0, 1, 2, 4, 8, 16])
            # ax.set_yticklabels([0, 1, 2, 4, 8, 16],fontsize=15)

            fig.subplots_adjust(left=0.03)
            filename = filename_prefix+'benchmarks_bar_' + _type + '_' + _timing
            # benchmarks_images['Bar `' + _type + '` benchmarks measuring `' + _timing + '` timing:'] = filename
            benchmarks_images[filename_prefix + ' Speedups'] = filename
            fig.savefig(chart_dir + filename + '.png', bbox_inches='tight')
            fig.savefig(chart_dir + filename + '.pdf', format='pdf', bbox_inches='tight')
            plt.close(fig)


def normalize_to_base(b, t_list):
    b = b if b > 0.001 else 0.001 # in case it was too fast
    s = []
    for t in t_list:
        t = t if t > 0.001 else 0.001 # in case it was too fast
        t = b / t
        s += [float(("%.5f" % t))]
    return s

def std_deviation(s):
    return np.array(s).std()

def std_error(s):
    return np.array(s).std()/math.sqrt(len(s))

def confidence_interval(s):
    z_critical = stats.norm.ppf(q = 0.95)
    return z_critical * std_error(s)

def error_bar_method(s):
    return std_error(s)
    # return std_deviation(s)
    # return confidence_interval(s)

def calculate_speedup(b, runs, is_list, is_base=False):
    if is_list:
        _err_bar = []
        _s = []
        for i in range(len(b)):
            _b = geomean(b[i])
            # print(runs)
            _s += [normalize_to_base(_b, runs[i])]
            _err_bar += [error_bar_method(_s[i])]
            if is_base:
                _s[i] = 1.
            else:
                _s[i] = geomean(_s[i])
        return _s, _err_bar
    else:
        _b = geomean(b)
        _s = normalize_to_base(_b, runs)
        _err_bar = error_bar_method(_s)
        if is_base:
            _s = 1.
        else:
            _s = geomean(_s)
        return _s, _err_bar

def do_speedups(benchmarks, base='CPython'):
    benchmarks_speedups = {}
    is_steps = False
    for _type in benchmarks:
        # print(_type)
        for _timing in benchmarks[_type]:
            # print(_timing)
            if _timing == "Steps":
                is_steps = True
            # if _timing == 'Warmup':
            #     continue
            for _interp in benchmarks[_type][_timing]:
                if _interp == base:
                    continue

                for _bench in benchmarks[_type][_timing][_interp]:
                    for _param in benchmarks[_type][_timing][_interp][_bench]:
                        b = benchmarks[_type][_timing][ base  ][_bench][_param]
                        runs = benchmarks[_type][_timing][_interp][_bench][_param]
                        s, err_bar = calculate_speedup(b, runs, is_steps)
                        benchmarks[_type][_timing][_interp][_bench][_param] = [ s, err_bar ]
                        # print('%s benchname: %s Time: %.2f  (%.2f)' % (_interp, _bench, benchmarks[_type][_timing][_interp][_bench][_param][0], benchmarks[_type][_timing][_interp][_bench][_param][1]))


            # set base timing to 1.0x
            _interp = base
            for _bench in benchmarks[_type][_timing][_interp]:
                for _param in benchmarks[_type][_timing][_interp][_bench]:
                    b = benchmarks[_type][_timing][ base  ][_bench][_param]
                    runs = benchmarks[_type][_timing][_interp][_bench][_param]
                    s, err_bar = calculate_speedup(b, runs, is_steps, True)
                    benchmarks[_type][_timing][_interp][_bench][_param] = [ s, err_bar ]

            is_steps = False

            # benchmarks[_type][_timing].pop(base)

def subtract_data_transfer(profile_data_single_core, benchmarks, new_single_core_str):
    _interp_cpu1 = 'MG-CPU1'
    for _type in benchmarks:
        for _timing in benchmarks[_type]:
            _result = benchmarks[_type][_timing][_interp_cpu1]
            _result = copy.deepcopy(_result)
            benchmarks[_type][_timing][new_single_core_str] = _result

    for _single_bench in profile_data_single_core:
        _sb_param = _single_bench.split('.')
        _sb = _sb_param[0]
        _param = _sb_param[1]
        for i in range(len(benchmarks[_type][_timing][new_single_core_str][_sb][_param])):
            benchmarks[_type][_timing][new_single_core_str][_sb][_param][i] -= profile_data_single_core[_single_bench][i]


def do_geomean(benchmarks_pure):
    benchmarks = copy.deepcopy(benchmarks_pure)

    for _type in benchmarks:
        for _timing in benchmarks[_type]:
            for _interp in benchmarks[_type][_timing]:
                for _bench in benchmarks[_type][_timing][_interp]:
                    for _param in benchmarks[_type][_timing][_interp][_bench]:
                        if isinstance(benchmarks[_type][_timing][_interp][_bench][_param][0], list):
                            _runs = benchmarks[_type][_timing][_interp][_bench][_param]
                            # _steps = [[_runs[i][j] for j in range(_run[i])] for i in range(len(_runs))]
                            _steps = [ geomean(benchmarks[_type][_timing][_interp][_bench][_param][i]) for i in range(len(_runs))]
                            benchmarks[_type][_timing][_interp][_bench][_param] = _steps
                        else:
                            g = geomean(benchmarks[_type][_timing][_interp][_bench][_param])
                            benchmarks[_type][_timing][_interp][_bench][_param] = g

    return benchmarks

def _read_results(systems_list, selected_benchmarks_list):
    global signed_date

    interpreter_list = []
    color_hatch_marker = {}
    ch = 0
    benchmarks = {}
    all_benchmarks_list = {}
    results_list = os.listdir(machine_results_dir)
    for f in sorted(results_list):
        if f == 'machine.json' or not f.endswith('.json'):
            continue

        result_tag = f.replace('.json','').split('-')
        # _interp = result_tag[1] # ZipPy
        _ver    = result_tag[0] # version
        _type   = result_tag[-5] + '-' + result_tag[-4] # normal, micro,..
        _timing = result_tag[-3] # peak
        _run    = result_tag[-1] # run #
        _commit = ''

        if 'profile' in _run:
            continue

        with open(machine_results_dir + '/' + f) as benchmark_file:
            bench_json = json.load(benchmark_file)
            # if 'commit_hash' not in bench_json or bench_json['commit_hash'] != commit_hash:
            #     continue

            _commit = bench_json['commit_hash']
            _interp = str(bench_json['params']['interpreter'])
            if systems_list:
                if _interp not in systems_list:
                    continue

            if signed_date == '':
                signed_date = ' (revision ' + commit_hash + ')'
                date = bench_json['date']
                date = datetime.datetime.fromtimestamp(int(date) / 1e3)
                signed_date = date.strftime("%Y-%d-%m %H:%M:%S") + signed_date

            if _type not in benchmarks:
                benchmarks[_type] = {}
                benchmarks[_type]['Warmup'] = {}
                benchmarks[_type]['Time'] = {}
                benchmarks[_type]['Steps'] = {}
                all_benchmarks_list[_type] = {}
                all_benchmarks_list[_type]['Warmup'] = {}
                all_benchmarks_list[_type]['Time'] = {}
                all_benchmarks_list[_type]['Steps'] = {}


            if _interp not in interpreters_versions:
                interpreters_versions[_interp] = _ver

            if _interp not in benchmarks[_type]['Warmup']:
                benchmarks[_type]['Warmup'][_interp] = {}
                benchmarks[_type]['Time'][_interp] = {}
                benchmarks[_type]['Steps'][_interp] = {}

            for _single_bench in bench_json['results']:
                _sb = str(_single_bench.replace( _type + '.', ''))

                if _sb in blacklist_bench:
                    continue

                if selected_benchmarks_list:
                    if _sb not in selected_benchmarks_list:
                        continue

                if _sb not in all_benchmarks_list[_type]['Warmup']:
                    all_benchmarks_list[_type]['Warmup'][_sb] = []
                    all_benchmarks_list[_type]['Time'][_sb] = []
                    all_benchmarks_list[_type]['Steps'][_sb] = [] # []

                for i in range(len(bench_json['results'][_single_bench]['params'][0])):
                    _param = str(bench_json['results'][_single_bench]['params'][0][i])
                    _time  = bench_json['results'][_single_bench]['result'][i]
                    if _time == None:
                        continue

                    if _sb in blacklist_bench_param and _param in blacklist_bench_param[_sb]:
                        continue

                    if _param not in all_benchmarks_list[_type]['Warmup'][_sb]:
                        all_benchmarks_list[_type]['Warmup'][_sb] += [_param]
                        all_benchmarks_list[_type]['Time'][_sb] += [_param]
                        all_benchmarks_list[_type]['Steps'][_sb] += [_param]

                    if _sb not in benchmarks[_type]['Warmup'][_interp]:
                        benchmarks[_type]['Warmup'][_interp][_sb] = {}
                        benchmarks[_type]['Time'][_interp][_sb] = {}
                        benchmarks[_type]['Steps'][_interp][_sb] = {}

                    _time = [float(_t) for _t in _time]
                    if _param not in benchmarks[_type]['Warmup'][_interp][_sb]:
                        benchmarks[_type]['Warmup'][_interp][_sb][_param] = []
                        benchmarks[_type]['Time'][_interp][_sb][_param] = []
                        benchmarks[_type]['Steps'][_interp][_sb][_param] = [[] for _t in range(len(_time))]

                    benchmarks[_type]['Warmup'][_interp][_sb][_param] += [_time[0]]
                    benchmarks[_type]['Time'][_interp][_sb][_param] += [min(_time)]
                    for _t in range(len(_time)):
                        benchmarks[_type]['Steps'][_interp][_sb][_param][_t] += [_time[_t]]


            if _interp not in interpreter_list:
                interpreter_list += [_interp]
                color_hatch_marker[_interp] = interpreter_color_hatch_marker[_interp]
                ch += 1

        if debug:
            print("{0}: {1} -- type: {2}  timing: {3} --run {4}  --commit {5}".format(_interp, _ver, _type, _timing, _run, _commit))

    return interpreter_list, color_hatch_marker, benchmarks, all_benchmarks_list


def plot_stack_bars(legends_names, benchmarks, total_times, profile_data, filename_prefix=''):
    # print(legends_names)
    # print(benchmarks)
    # print(profile_data)

    f, ax = plt.subplots(1, figsize=(12,5))

    # Set bar width at 1
    bar_width = .25

    # positions of the left bar-boundaries
    bar_l_1 = [i for i in range(len(benchmarks))]
    bar_l_2 = [i + bar_width + .1 for i in range(len(benchmarks))]

    # positions of the x-axis ticks (center of the bars as bar labels)
    # tick_pos = [(i + j) / 2 for i,j in zip(bar_l_1, bar_l_2)]
    tick_pos = [i+(bar_width) for i in bar_l_1]

    num_benchmarks = len(benchmarks)
    totals_1 = [0. for i in range(num_benchmarks)]
    totals_2 = [0. for i in range(num_benchmarks)]
    legends_bars = []
    legends_bars_2 = []
    for i in range(len(legends_names)):
        legend_list_1 = [profile_data["profile1"][b][i] for b in range(num_benchmarks)]
        legend_list_2 = [profile_data["profile2"][b][i] for b in range(num_benchmarks)]
        legends_bars += [ax.bar(bar_l_1,
                legend_list_1,
                bottom=totals_1,
                label='Post Score',
                alpha=0.9,
                color=list_color_hatch_marker[i][0],
                hatch=list_color_hatch_marker[i][1],
                width=bar_width,
                edgecolor = "none",#'black'
                linewidth=0
                )]
        legends_bars_2 += [ax.bar(bar_l_2,
                legend_list_2,
                bottom=totals_2,
                label='Post Score',
                alpha=0.9,
                color=list_color_hatch_marker[i][0],
                hatch=list_color_hatch_marker[i][1],
                width=bar_width,
                edgecolor = "none",#'black'
                linewidth=0
                )]
        totals_1 = [i+j for i,j in zip(legend_list_1, totals_1)]
        totals_2 = [i+j for i,j in zip(legend_list_2, totals_2)]

    height_len = 1.02
    def autolabel(rects, label):
        """
        Attach a text label above each bar displaying its height
        """
        for rect in rects:
            height = rect.get_height()
            ax.text(rect.get_x() + rect.get_width()/2., 100 * height_len,
                    label,
                    ha='center', va='bottom')

    autolabel(legends_bars[-1], 'C')
    autolabel(legends_bars_2[-1], 'P')

    plt.xticks(tick_pos, benchmarks)
    ax.set_ylabel("Percentage")
    ax.set_xlabel("")
    # ax.legend(legends_bars, legends_names + ['W','H'], fontsize='medium', mode="expand", ncol=3, bbox_to_anchor=(0., 1.02, 1., .102), loc=3, borderaxespad=0.)
    from matplotlib.patches import Rectangle
    extra = Rectangle((0, 0), 1, 1, fc="w", fill=False, edgecolor='none', linewidth=0)
    ax.legend(legends_bars + [extra, extra], legends_names + ['C: Cold Run','P: Peak'], fontsize='medium', mode="expand", ncol=3, bbox_to_anchor=(0., 1.02, 1., .102), loc=3, borderaxespad=0.)

    # Let the borders of the graphic
    plt.xlim([-.5, num_benchmarks + bar_width])
    # print([min(tick_pos) - bar_width + 1, max(tick_pos)+ bar_width + 1])
    plt.ylim(-10, 110)

    # rotate axis labels
    plt.setp(plt.gca().get_xticklabels(), rotation=45, horizontalalignment='right')

    # shot plot
    filename = filename_prefix+"profile"
    benchmarks_images[filename_prefix + ' Breakdown passes'] = filename
    f.savefig(chart_dir + filename + '.png', bbox_inches='tight')
    f.savefig(chart_dir + filename + '.pdf', format='pdf', bbox_inches='tight')
    plt.close(f)



# bench_profile_data
# core_execution_time
# data_Transfer_time
# dependence_time
# bound_check_time
# translation_time
# compilation_time
# opencl_translation_time
# unbox_time
profile_files = ["profile1", "profile2"]#, "profileautodevice", "cpu1profile"]
def _read_profile_results():

    legends_names = ["Guards Optimization",
                     "Unboxing",
                     "Dependence Analysis",
                     "Bounds Check Optimization",
                     "Compilation",
                     "Data Transfer",
                     "Kernel Execution"]

    benchmarks_profiles = {}
    benchmarks_names = []
    benchmarks_total_time = []
    results_list = os.listdir(machine_results_dir)
    profile_data = {}
    for f in sorted(results_list):
        if f == 'machine.json' or not f.endswith('.json'):
            continue

        result_tag = f.replace('.json','').split('-')
        _type   = "hpc-rodinia"
        _run    = result_tag[-1] # run #
        if 'profile' in _run:
            # if _run in profile_files:
            with open(machine_results_dir + '/' + f) as benchmark_file:
                profile_data[_run] = json.load(benchmark_file)


    benchmarks_profiles[profile_files[0]] = []
    benchmarks_profiles[profile_files[1]] = []
    # benchmarks_profiles[profile_files[3]] = {}
    _profile1 = profile_data[profile_files[0]]
    _profile2 = profile_data[profile_files[1]]
    # _profile_autodevice = profile_data[profile_files[2]]

    for _single_bench in sorted(_profile1['profiling']):
        _sb = str(_single_bench.replace( _type + '.', ''))
        if _sb in blacklist_bench:
            continue
        _largest_parm = benchmarks_list[_type][1][_sb][-2]

        _runs = []
        # Data_Transfer = 0
        # for i in range(1,9):
        #     _profile_cpu1 = profile_data[profile_files[3] + ('%d' % i)]
        #     bench_profile_data  = _profile_cpu1['profiling'][_single_bench][_largest_parm]
        #     Data_Transfer = abs(int(bench_profile_data[ "data_Transfer_time"      ]) - Data_Transfer)
        #     _runs += [float(Data_Transfer)/1000]
        #
        # benchmarks_profiles[profile_files[3]][_sb + '.' + _largest_parm] = _runs

    # print("benchmark, kernels, Kernel Execs, Loop count, CPU, GPU")

    for _single_bench in sorted(_profile1['profiling']):
        _sb = str(_single_bench.replace( _type + '.', ''))
        if _sb in blacklist_bench:
            continue
        _largest_parm = benchmarks_list[_type][1][_sb][-2]
        bench_profile_data  = _profile1['profiling'][_single_bench][_largest_parm]

        Speculation_Elimination = int(bench_profile_data[ "translation_time"        ])
        Unboxing = int(bench_profile_data[ "unbox_time"              ])
        Dependence_Analysis = int(bench_profile_data[ "dependence_time"         ])
        Bound_Check = int(bench_profile_data[ "bound_check_time"        ])
        Compilation = int(bench_profile_data[ "code_generation_time" ]) + int(bench_profile_data[ "compilation_time"        ])
        Data_Transfer = int(bench_profile_data[ "data_transfer_time"      ])
        Kernel_Execution = int(bench_profile_data[ "core_execution_time"     ])

        bench_profile_list  = [Speculation_Elimination]
        bench_profile_list += [Unboxing]
        bench_profile_list += [Dependence_Analysis]
        bench_profile_list += [Bound_Check]
        bench_profile_list += [Compilation]
        bench_profile_list += [Data_Transfer]
        bench_profile_list += [Kernel_Execution]
        total_time          = sum(bench_profile_list)
        total = [total_time for i in range(len(bench_profile_list))]

        bench_profile_list = [float(i) / j * 100 for  i,j in zip(bench_profile_list, total)]
        if (100 - sum(bench_profile_list)) > 1:
            print("%s: %.4f" % (_sb, sum(bench_profile_list)))
        benchmarks_profiles[profile_files[0]] += [bench_profile_list]
        benchmarks_names += [_sb]
        benchmarks_total_time += [total_time]

        counts  = [_sb]
        counts += [int(bench_profile_data["total_generated_kernels" ])]
        counts += [int(bench_profile_data["total_kernels_executions"])]

        loopcount =  int(bench_profile_data["total_parallel_loops"    ])
        power_of_10 = int(math.log(loopcount, 10))
        strloopcount = "%.1f x 10%d" % (float(loopcount) / (10**power_of_10), power_of_10)
        counts += [strloopcount]


        bench_profile_data  = _profile2['profiling'][_single_bench][_largest_parm]

        # Speculation_Elimination -= int(bench_profile_data[ "translation_time"        ])
        Speculation_Elimination = 0
        Unboxing -= int(bench_profile_data[ "unbox_time"              ])
        # Dependence_Analysis -= int(bench_profile_data[ "dependence_time"         ])
        Dependence_Analysis = 0
        Bound_Check -= int(bench_profile_data[ "bound_check_time"        ])
        # Compilation -= int(bench_profile_data[ "opencl_translation_time" ]) + int(bench_profile_data[ "compilation_time"        ])
        Compilation = 0
        Data_Transfer -= int(bench_profile_data[ "data_transfer_time"      ])
        Kernel_Execution -= int(bench_profile_data[ "core_execution_time"     ])

        bench_profile_list  = [abs( Speculation_Elimination )  ]
        bench_profile_list += [abs( Unboxing                )  ]
        bench_profile_list += [abs( Dependence_Analysis     )  ]
        bench_profile_list += [abs( Bound_Check             )  ]
        bench_profile_list += [abs( Compilation             )  ]
        bench_profile_list += [abs( Data_Transfer           )  ]
        bench_profile_list += [abs( Kernel_Execution        )  ]
        total_time          = sum(bench_profile_list)
        total = [total_time for i in range(len(bench_profile_list))]

        bench_profile_list = [float(i) / j * 100 for  i,j in zip(bench_profile_list, total)]
        if (100 - sum(bench_profile_list)) > 1:
            print("%s: %.4f" % (_sb, sum(bench_profile_list)))

        benchmarks_profiles[profile_files[1]] += [bench_profile_list]

        # bench_profile_data  = _profile_autodevice['profiling'][_single_bench][_largest_parm]
        # devices = bench_profile_data["final_execution_device"  ]
        # expr_count = r"(OpenCL (?P<DEVICE>[a-zA-Z0-9\.\-\_]+): (?P<NAME>(.*?)):(?P<COUNT>[0-9]+))+"
        # m = re.findall(expr_count, devices)
        #
        # counts += [0,0]
        # for d in m:
        #     if "CPU" in d[1]:
        #         counts[-2] = int(d[4])
        #     if "GPU" in d[1]:
        #         counts[-1] = int(d[4])
        #
        # print("%s, %d, %d, %s, %d, %d" % tuple(counts))

    return legends_names, benchmarks_names, benchmarks_total_time, benchmarks_profiles

def add_figures_to_json_file(filename='figures.json'):
    figures_info = {}
    is_exists = False
    if os.path.isfile(chart_dir + filename):
        with open(chart_dir + filename, 'r') as json_figures:
            figures_info = json.load(json_figures)
            is_exists = True

    if is_exists:
        print('Updating %s with new figures' % filename)
    else:
        print('Creating %s with new figures' % filename)

    figures_info.update(benchmarks_images)

    dump = json.dumps(figures_info, sort_keys = True, indent = 4)
    with open(chart_dir + filename, "w") as txtfile:
        txtfile.write(dump)

def generate_ecoop_page_result(filename='figures.json'):
    figures_info = {}
    is_exists = False
    with open(chart_dir + filename, 'r') as json_figures:
        figures_info = json.load(json_figures)
        markdown_ecoop(figures_info)
        is_exists = True

    if is_exists:
        print('Page is ready')
    else:
        print('JSON file %s does not exists!' % filename)


def _asv_chart(args):
    if importerror:
        mx.abort("numpy, matplotlib, or functools library is missing.")
    base = 'CPython'
    try:
        idx = args.index("--")
        args = args[:idx]
    except ValueError:
        pass

    parser = ArgumentParser(
        prog="mx asv-chart",
        add_help=False,
        usage="mx asv-chart <options>",
        formatter_class=RawTextHelpFormatter)
    parser.add_argument(
        "--base", nargs="?", default=None,
        help="Select base benchmark.")
    parser.add_argument(
        "--scales", action="store_true", default=None,
        help="Generate scales charts all parametized benchmarks.")
    parser.add_argument(
        "--bars", action="store_true", default=None,
        help="Generate bars charts for largest parameters of the benchmarks.")
    parser.add_argument(
        "--bars-kdm", action="store_true", default=None,
        help="Generate bars charts for KDM.")
    parser.add_argument(
        "--steps", action="store_true", default=None,
        help="Generate steps charts all parametized benchmarks.")
    parser.add_argument(
        "--cpu-cores-steps", action="store_true", default=None,
        help="Generate steps charts all parametized benchmarks.")
    parser.add_argument(
        "--single-core-cpu", action="store_true", default=None,
        help="Add single core cpu without data transfer.")
    parser.add_argument(
        "--profile", action="store_true", default=None,
        help="Generate stack bars charts for all benchmarks.")
    parser.add_argument(
        "--benchmarks", nargs="?", default=None,
        help="Select a subset of benchmarks seperated with a comma, e.g --benchmarks bfs,mm,lud")
    parser.add_argument(
        "--systems", nargs="?", default=None,
        help="Select the systems which will be compared with each other seperated with a comma, e.g --systems ZipPy,PyPy3,CPython")
    parser.add_argument(
        "--prefix-desc-text", nargs="?", default=None,
        help="Prefix figure description. e.g. Figure12")
    # parser.add_argument(
    #     "--json-figure-file", nargs="?", default=None,
    #     help="Name of the json file that stores figure(s) information")
    parser.add_argument(
        "--ecoop-result-page", action="store_true", default=None,
        help="Generate ECOOP result page using the collected information in json file.")
    parser.add_argument(
        "--geomean", action="store_true", default=None,
        help="Generate stack bars charts for all benchmarks.")
    parser.add_argument(
        "-h", "--help", action="store_true", default=None,
        help="Show usage information.")
    args = parser.parse_args(args)

    if args.base:
        base = args.base

    if not exists(asv_env + '/graphs'):
        os.mkdir(asv_env + '/graphs')

    if not exists(chart_dir):
        os.mkdir(chart_dir)

    if args.ecoop_result_page:
        generate_ecoop_page_result()
        return

    filename_prefix = ''
    if args.prefix_desc_text:
        filename_prefix = args.prefix_desc_text


    if args.profile:
        legends_names, benchmarks, total_times, profile_data = _read_profile_results()
        plot_stack_bars(legends_names, benchmarks, total_times, profile_data, filename_prefix=filename_prefix)
        add_figures_to_json_file()
        return

    systems_list = None
    if args.systems:
        systems_list = args.systems.split(',')

    selected_benchmarks_list = None
    if args.benchmarks:
        selected_benchmarks_list = args.benchmarks.split(',')

    single_benchmark = None
    if args.geomean:
        single_benchmark = 'GeoMean'

    interpreter_list, color_hatch_marker, benchmarks, all_benchmarks_list = _read_results(systems_list, selected_benchmarks_list)
    if base not in interpreter_list:
        mx.abort("Base interpreter {0} has no benchmark results.".format(base))

    if args.single_core_cpu:
        new_single_core_str = 'MG-CPU1-NoDT'
        legends_names, benchmarks_names, total_times, profile_data = _read_profile_results()
        subtract_data_transfer(profile_data[profile_files[3]], benchmarks, new_single_core_str)
        interpreter_list += [new_single_core_str]

    benchmarks_copy = copy.deepcopy(benchmarks)
    benchmarks_geomean = do_geomean(benchmarks_copy)
    dump = json.dumps(benchmarks_geomean, sort_keys = True, indent = 4)
    with open("benchmarks-geomean.json", "w") as txtfile:
        txtfile.write(dump)

    do_speedups(benchmarks_copy, base)
    dump = json.dumps(benchmarks_copy, sort_keys = True, indent = 4)
    with open("benchmarks-speedups-ci.json", "w") as txtfile:
        txtfile.write(dump)
    # markdown_each(benchmarks, base, benchmarks_stats_each)

    process_plot_bars = False
    if args.bars or args.bars_kdm:
        process_plot_bars = True
        process_plot_bars_kdm = True if args.bars_kdm else False

    if process_plot_bars:
        # we might need to always run this
        sl = systems_list if systems_list else interpreter_list
        process_plot(benchmarks_copy, all_benchmarks_list, sl, color_hatch_marker, base, process_plot_bars_kdm, filename_prefix=filename_prefix)
    if args.scales:
        plot_scales(benchmarks_copy, all_benchmarks_list, base, filename_prefix=filename_prefix)

    if args.steps:
        plot_steps(benchmarks_copy, all_benchmarks_list, base, interpreter_list_steps_ordered, single_benchmark=single_benchmark, filename_prefix=filename_prefix)

    if args.cpu_cores_steps:
        plot_steps(benchmarks_copy, all_benchmarks_list, base, interpreter_list_cpu_cores_steps_ordered, 'cores_steps', single_benchmark=single_benchmark, filename_prefix=filename_prefix)

    add_figures_to_json_file()
    markdown_readme(base, signed_date, interpreters_versions, benchmarks_images, benchmarks_stats_each, benchmarks_stats_types, benchmarks_stats_overall)

mx.update_commands(mx.suite('zippy'), {
    'asv-chart' : [_asv_chart, 'Generate chart for benchmarked results.'],
})
