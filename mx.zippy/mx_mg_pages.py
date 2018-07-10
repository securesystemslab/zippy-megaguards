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

strapdown_md_to_html = [
 '''
<!DOCTYPE html>
<html>
<title>Results</title>

<xmp theme="journal" style="display:none;">

 ''',
 '''

</xmp>

<script src="http://strapdownjs.com/v/0.2/strapdown.js"></script>
</html>
 '''
]

def markdown_ecoop(benchmarks_images, filename='ECOOP18'):
    markdown_content = ''
    dump = '# Benchmark result for machine (' + machine_name + '):\n\n'
    markdown_content += dump

    dump = '\n# Graphs:\n'
    markdown_content += dump

    for title in sorted(benchmarks_images):
        dump  = '\n## ' + title + '\n\n'
        image_path = benchmarks_images[title]
        dump += '\n' + '[![image](' + image_path + '.png' + ')](' + image_path + '.pdf' + ')\n\n'
        markdown_content += dump

    dump = '# Done'
    markdown_content += dump
    with open(chart_dir + filename + '.md', "w") as readme:
        readme.write(markdown_content)

    with open(chart_dir + filename + '.html', "w") as readme:
        readme.write(strapdown_md_to_html[0])
        readme.write(markdown_content)
        readme.write(strapdown_md_to_html[1])

    print('Page is ready at %s.html and .md' % (chart_dir + filename))


def markdown_readme(base, signed_date, interpreters_versions, benchmarks_images, benchmarks_stats_each, benchmarks_stats_types, benchmarks_stats_overall):
    markdown_content = ''
    dump = '# Benchmark result for machine (' + machine_name + '):\n\n'
    markdown_content += dump

    dump = '\nResults are as of ' + signed_date + '\n'
    markdown_content += dump

    dump = '\nList of interpreters:\n\n'
    markdown_content += dump
    for _interp in interpreters_versions:
        dump = '* ' + _interp + ': `' + interpreters_versions[_interp] + '`\n'
        markdown_content += dump

    dump = '\n\n> Normalized to: ' + base + '\n\n'
    markdown_content += dump

    dump = '\n# Graphs:\n'
    markdown_content += dump

    for title in sorted(benchmarks_images):
        dump  = '\n## ' + title + '\n\n'
        image_path = benchmarks_images[title]
        dump += '\n' + '[![image](' + image_path + '.png' + ')](' + image_path + '.pdf' + ')\n\n'
        markdown_content += dump


    dump = '\n# Statistics:\n'
    markdown_content += dump


    dump = '\n## Overall performance:\n'
    markdown_content += dump
    for _interp in benchmarks_stats_overall:
        for _timing in benchmarks_stats_overall[_interp]:
            dump = _interp + ': Overall `' + _timing + '` performance :: '
            dump += 'Geometeric mean: `' + ("%.3f" % geomean(benchmarks_stats_overall[_interp][_timing])    )  + 'x`, '
            dump += 'Average: `'         + ("%.3f" % np.average(benchmarks_stats_overall[_interp][_timing]) )  + 'x`, '
            dump += 'Maximum: `'         + ("%.3f" % max(benchmarks_stats_overall[_interp][_timing])        )  + 'x`\n\n'
            markdown_content += dump

    dump = '\n## Benchmarks performance:\n'
    markdown_content += dump
    for _type in benchmarks_stats_types:
        dump = '\n### `' + _type + '` performance:\n'
        markdown_content += dump
        for _timing in benchmarks_stats_types[_type]:
            dump = '\n##### `' + _timing + '` measurement:\n'
            markdown_content += dump
            for _measurement in benchmarks_stats_types[_type][_timing]:
                dump = _measurement
                markdown_content += dump

    dump = '\n## Each Benchmark performance:\n'
    markdown_content += dump

    for _type in benchmarks_stats_each:
        dump = '\n### `' + _type + '` performance:\n'
        markdown_content += dump
        for _timing in benchmarks_stats_each[_type]:
            dump = '\n##### `' + _timing + '` measurement:\n'
            markdown_content += dump
            for _bench in benchmarks_stats_each[_type][_timing]:
                _bench_txt = '`' + _bench + '` '
                if isinstance(benchmarks_stats_each[_type][_timing][_bench], dict):
                    for _param in benchmarks_stats_each[_type][_timing][_bench]:
                        dump = _bench_txt + '`' + _param + '`: ' + benchmarks_stats_each[_type][_timing][_bench][_param] + '\n\n'
                        markdown_content += dump
                else:
                    dump = _bench_txt + ': ' + benchmarks_stats_each[_type][_timing][_bench] + '\n\n'
                    markdown_content += dump

    dump = '# Done'
    markdown_content += dump
    with open(chart_dir + 'README.md', "w") as readme:
        readme.write(markdown_content)

    with open(chart_dir + 'README.html', "w") as readme:
        readme.write(strapdown_md_to_html[0])
        readme.write(markdown_content)
        readme.write(strapdown_md_to_html[1])

def markdown_overall_speedups(_type, _timing, r_benchmarks, benchmarks_stats_types, benchmarks_stats_overall):
    txt_geomean = ' Geometeric mean :: '
    txt_avg     = ' Average         :: '
    txt_max     = ' Maximum         :: '
    for _interp in r_benchmarks:
        txt_geomean += _interp + ': `' + ("%.3f" % geomean(r_benchmarks[_interp])   ) + 'x`, '
        txt_avg     += _interp + ': `' + ("%.3f" % np.average(r_benchmarks[_interp])) + 'x`, '
        txt_max     += _interp + ': `' + ("%.3f" % max(r_benchmarks[_interp])       ) + 'x`, '
        if _interp not in benchmarks_stats_overall:
            benchmarks_stats_overall[_interp] = {}
        if _timing not in benchmarks_stats_overall[_interp]:
            if _timing == 'Steps':
                continue

            benchmarks_stats_overall[_interp][_timing] = []
        benchmarks_stats_overall[_interp][_timing] += r_benchmarks[_interp]

    txt_geomean += '\n\n'
    txt_avg     += '\n\n'
    txt_max     += '\n\n'
    if _type not in benchmarks_stats_types:
        benchmarks_stats_types[_type] = {}
    benchmarks_stats_types[_type][_timing] = [txt_geomean, txt_avg, txt_max]

def markdown_each(benchmarks, base, benchmarks_stats_each):
    for _type in benchmarks:
        if _type not in benchmarks_stats_each:
            benchmarks_stats_each[_type] = {}

        for _timing in benchmarks[_type]:
            if _timing == 'Steps':
                continue

            if _timing not in benchmarks_stats_each[_type]:
                benchmarks_stats_each[_type][_timing] = {}

            for _interp in benchmarks[_type][_timing]:
                for _bench in benchmarks[_type][_timing][_interp]:
                    if _bench not in benchmarks_stats_each[_type][_timing]:
                        benchmarks_stats_each[_type][_timing][_bench] = {}

                    for _param in benchmarks[_type][_timing][_interp][_bench]:
                        if _param not in benchmarks_stats_each[_type][_timing][_bench]:
                            benchmarks_stats_each[_type][_timing][_bench][_param] = base + ': `1.00x`, '

                        s = benchmarks[_type][_timing][_interp][_bench][_param]
                        benchmarks_stats_each[_type][_timing][_bench][_param] += _interp + ': `' + ("%.3f" % s) + '`, '
