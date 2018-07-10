from os.path import join, sep
import mx

_suite = mx.suite('zippy')

core_junit_test_classes_opencl = ['ForTests', 'DDTests', 'FunCallTests', 'ReductionTests']
all_junit_test_classes_opencl = core_junit_test_classes_opencl

core_junit_test_classes_truffle = ['TruffleDDTests','TruffleForTests','TruffleFunCallTests']
all_junit_test_classes_truffle = core_junit_test_classes_truffle


def _mg_opencl_test_package():
    return 'edu.uci.megaguards.test.parallel'

def _mg_truffle_test_package():
    return 'edu.uci.megaguards.test.truffle'

def _mg_opencl_test_subpackage(name):
    return '.'.join((_mg_opencl_test_package(), name))

def _mg_truffle_test_subpackage(name):
    return '.'.join((_mg_truffle_test_package(), name))

def _mg_opencl_core_generated_unit_tests():
    return ','.join(map(_mg_opencl_test_subpackage, core_junit_test_classes_opencl))

def _mg_truffle_core_generated_unit_tests():
    return ','.join(map(_mg_truffle_test_subpackage, core_junit_test_classes_truffle))

def _mg_core_unit_tests():
    return ','.join([_mg_opencl_core_generated_unit_tests(), _mg_truffle_core_generated_unit_tests()])

def junit_mg_core(args):
    return mx.command_function('junit')(['--tests', _mg_core_unit_tests()] + args)

def _mg_all_generated_unit_tests():
    return ','.join(map(_mg_opencl_test_subpackage, all_junit_test_classes_opencl)) + \
                ',' + ','.join(map(_mg_truffle_test_subpackage, all_junit_test_classes_truffle))

def _mg_all_unit_tests():
    return ','.join([_mg_all_generated_unit_tests()])

def junit_mg_all(args):
    return mx.command_function('junit')(['--tests', _mg_all_unit_tests()] + args)

mx.update_commands(_suite, {
    'junit-mg-core'         : [junit_mg_core, ['options']],
    'junit-mg'              : [junit_mg_all, ['options']],
})
