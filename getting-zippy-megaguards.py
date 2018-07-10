import os, sys
import re
from subprocess import Popen, PIPE
from threading import Thread

global_verbose = True if 'VERBOSE' in os.environ and os.environ['VERBOSE'] == '1' else False

noerrors = True

zippy_megaguards_repo = "https://github.com/securesystemslab/zippy-megaguards.git"
zippy_megaguards_branch = "master"
zippy_megaguards_name = "zippy-megaguards"
megaguards_name = "megaguards"

jvmci_ver = '0.46'
jvmci_jdk_ver = '1.8.0_172-jvmci-%s' % jvmci_ver
jvmci_jdk_ver_tar = '8u172-jvmci-%s' % jvmci_ver


current_path = os.getcwd()
mx_path = current_path + os.sep + 'mx'
jvmci_path = [
    current_path + os.sep + 'labsjdk%s' % jvmci_jdk_ver, # Oracle
    current_path + os.sep + 'openjdk%s' % jvmci_jdk_ver  # OpenJDK
]
zippy_megaguards_path = current_path + os.sep + zippy_megaguards_name
megaguards_path = current_path + os.sep + megaguards_name


list_files = os.listdir(current_path)
for f in list_files:
    if f.startswith('labsjdk1.8') and os.path.isdir(f):
        jvmci_path[0] = current_path + os.sep + f

system_packages = [
    "build-essential",
    "git",
    "wget",
    "curl",
    "ocl-icd-opencl-dev",
    "clinfo"
]

reminders = []

_ansi_color_table = { 'red' : '31', 'green' : '32', 'yellow' : '33', 'magenta' : '35' }
def colorize(msg, color='red'):
    coloring = '\033[' + _ansi_color_table[color] + ';1m'
    isUnix = sys.platform in ['linux', 'linux2', 'darwin', 'freebsd']
    if isUnix and hasattr(sys.stderr, 'isatty') and sys.stderr.isatty():
        return coloring + msg + '\033[0m'
    return msg

def print_ok(msg):
    ok = colorize('   OK  ', 'green')
    print('[' + ok + '] ' + msg)

def print_progress(msg):
    print('[  ...  ] ' + msg)

def print_error(msg):
    err = colorize(' ERROR ', 'red')
    print('[' + err + '] ' + msg)

def print_warn(msg):
    w = colorize('WARNING', 'magenta')
    print('[' + w + '] ' + msg)

def print_info(msg):
    i = colorize(' INFO  ', 'yellow')
    print('[' + i + '] ' + msg)

def print_question(msg):
    i = colorize('   ?   ', 'yellow')
    return raw_input('[' + i + '] ' + msg)

def execute_shell(cmd, cwd=None, check_only=False, verbose=False, directly=False):
    global noerrors
    print_progress('Executing: %s' % ' '.join(map(str, cmd)))
    if cwd is not None:
        print_progress('Path: %s' % cwd)
    try:
        if directly:
            p = Popen(cmd, cwd=cwd)
        else:
            p = Popen(cmd, cwd=cwd, stdout=PIPE, stderr=PIPE)
    except:
        if check_only:
            return 1
        raise
    o = ''
    e = ''
    retcode = 0
    if (verbose or global_verbose) and not directly:
        def teeOutput(s, f):
            for l in iter(s.readline, ''):
                print l,
                f[0] += l
            s.close()
        _o = ['']
        _e = ['']
        threads = [
            Thread(target=teeOutput, args=(p.stdout, _o)),
            Thread(target=teeOutput, args=(p.stderr, _e))
        ]
        for t in threads:
            t.start()
        while any([t.is_alive() for t in threads]):
            for t in threads:
                t.join(10)
        o = _o[0]
        e = _e[0]
        retcode = p.wait()
    else:
        o, e = p.communicate()
        retcode = p.returncode

    if check_only:
        return retcode
    if retcode != 0:
        print_error('Please fix this and re-run this script')
        noerrors = False
        print(e)
        print_error('return code: %d' % retcode)
        exit(1)
    else:
        print_ok('Success')
    return retcode, o, e

def ask(msg, boolean=True, option=''):
    q = 'y/n' if boolean else option
    q = ' [%s]' % q
    answer = print_question(msg + q)
    if boolean:
        answer = answer.lower()
        if answer == 'y' or answer == 'yes':
            return True
        elif answer == 'n' or answer == 'no':
            return False
        else:
            print_info("Please answer using %s" % q)
            return ask(msg, boolean, _yes, _no)
    elif answer != '':
        return answer

    return option

def end_script():
    for r in reminders:
        print_info(r)
    print_error('We cannot proceed.. please fix all errors and retry again')
    print_error("You can set VERBOSE=1 to print all stdout and stderr\n\n\t$ export VERBOSE=1\n")
    exit(1)

def loadEnv(path):
    envre = re.compile(r'''^([^=]+)=\s*(?:["'])(.+?)(?:["'])\s*$''')
    result = {}
    try:
        with open(path) as fp:
            for line in fp:
                match = envre.match(line)
                if match is not None:
                    result[match.group(1)] = match.group(2)
        return result
    except:
        pass
    return None

def update_env_file(path, envVars):
        _lines = []
        if not os.path.exists(path):
            with open(path,'w') as fp:
                fp.write("# Environment variables\n")

        with open(path,'r') as fp:
            for line in fp:
                _lines += [line]
        added = False
        lines = []
        for line in _lines:
            if '=' in line and '#' not in line:
                line = line.replace('\n','').split('=')
                var = line[0]
                varpath = line[1]
                if var in envVars:
                    if varpath != envVars[var]:
                        print('%s != %s' % (varpath, envVars[var]))
                        print(line)
                        var = '# ' + var
                    else:
                        envVars.pop(var)
                lines += [var + '=' + varpath + '\n']
            else:
                lines += [line]

        for v in envVars:
            lines += [v + "=" + envVars[v] + '\n']

        with open(path,'w') as fp:
            for line in lines:
                fp.write(line)

print_info('This script is compatible with Ubuntu x86 16.04 64-bit')
os_env = loadEnv(os.sep + 'etc' + os.sep + 'os-release')
if not os_env or 'VERSION_ID' not in os_env or os_env['VERSION_ID'] != '16.04':
    print_warn("ZipPy+MegaGuards has not been tested for your system")

system_packages_str = ''
has_missing_packages = False
missing_packages = []
for c in system_packages:
    cmd = ['dpkg', '-l', c]
    retcode = execute_shell(cmd, check_only=True)
    if retcode != 0:
        has_missing_packages = True
        missing_packages += [c]
        system_packages_str += '\t' + ' '.join(map(str, c)) + '\n'

if has_missing_packages:
    msg = "First, we need to install the following packages:\n%s\nThese require 'sudo' privilege. Do you want to continue?" % sudo_cmds_str
    if ask(msg):
        cmd = ["sudo", "apt-get", "update"]
        execute_shell(cmd)
        for c in missing_packages:
            execute_shell(["sudo", "apt-get", "install", c])
    else:
        end_script()

print_ok("All system packages are installed")

print_info('Checking if mx is installed in your system')
cmd = ['mx', 'version']
retcode = execute_shell(cmd, check_only=True)
mx_exists = True if retcode == 0 else False
if not mx_exists:
    print_progress('Trying to locate mx at %s' % mx_path)
    if not os.path.isfile(mx_path + os.sep + 'LICENSE'):
        print_progress('Could not find mx. Trying to download it')
        cmd = ["git", "clone", "https://github.com/graalvm/mx.git"]
        execute_shell(cmd, verbose=True)
    shell_conf = ''
    if 'SHELL' in os.environ:
        shell_conf = '.' + os.environ.get('SHELL').split(os.sep)[-1] + 'rc'
        shell_conf = os.getenv('HOME') + os.sep + shell_conf
        if os.path.isfile(shell_conf):
            print_ok("We will add mx to $PATH variable in %s" % shell_conf)
        else:
            print_info("We couldn't find the config file for your shell")
            path = ask("Please provide the full path for your\nshell config file:", boolean=False, option=os.sep + 'path' + os.sep + 'to')
            if not os.path.isfile(path):
                print_error("shell config file provided doesn't exist")
                end_script()
            shell_conf = path

    mx_envpath = 'export PATH=$PATH:' + mx_path
    mx_envpath_exists = False
    with open(shell_conf, 'r') as fp:
        for l in fp:
            if l.replace('\n', '') == mx_envpath:
                mx_envpath_exists = True
                break
    if not mx_envpath_exists:
        with open(shell_conf, 'a') as fp:
            fp.write('\n# mx\n')
            fp.write(mx_envpath + '\n')
        reminders += ["Please execute\n\n\t$ source %s\nto reload $PATH variable" % shell_conf]
        os.environ['PATH'] = os.environ['PATH'] + ':' + mx_path
    else:
        print_info("Please re-run this script after you execute\n\n\t$ source %s\n" % shell_conf)
        end_script()


print_ok("mx is installed")

jdk_found = -1
print_progress('Trying to locate Oracle JDK with JVMCI at %s' % jvmci_path[0])
if not os.path.isfile(jvmci_path[0] + os.sep + 'bin' + os.sep + 'java'):
    print_progress('Trying to locate OpenJDK with JVMCI at %s' % jvmci_path[1])
    if not os.path.isfile(jvmci_path[1] + os.sep + 'bin' + os.sep + 'java'):
        print_progress('Could not find a JDK with JVMCI.')
    else:
        jdk_found = 1
else:
    jdk_found = 0

if jdk_found == -1:
    jdk_options  = "Which JDK do you want?\n"
    jdk_options += "[1] Oracle JDK with JVMCI %s (fast but proprietary)\n" % jvmci_jdk_ver
    jdk_options += "[2] OpenJDK JDK with JVMCI %s (Open Source)\n" % jvmci_jdk_ver
    jdk_options += "Your choice"
    jdk_choice = ask(jdk_options, boolean=False, option='1')
    default_ver = ''
    if jdk_choice == '1':
        default_ver = 'labsjdk-%s-linux-amd64.tar.gz' % jvmci_jdk_ver_tar
        if not os.path.isfile(default_ver):
            print_info('Please download it from http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html')
            print_info('to this location: %s' % (current_path + os.sep + default_ver))
            print_info('Then, re-run this script')
            end_script()
        else:
            jdk_found = 0

    elif jdk_choice == '2':
        default_ver = 'openjdk-%s-linux-amd64.tar.gz' % jvmci_jdk_ver_tar
        jdk_found = 1
        if not os.path.isfile(default_ver):
            cmd = ['wget', 'https://github.com/graalvm/openjdk8-jvmci-builder/releases/download/jvmci-%s/openjdk-%s-linux-amd64.tar.gz' % (jvmci_ver, jvmci_jdk_ver_tar)]
            execute_shell(cmd, verbose=True)
    else:
        print_error('Please re-run this script and choose a JDK')
        end_script()

    cmd = ["tar", "-xzf", default_ver]
    execute_shell(cmd)

print_ok("JDK with JVMCI %s is installed" % jvmci_path[jdk_found])

print_info("Checking ZipPy+MegaGuards..")
if not os.path.isfile(zippy_megaguards_path + os.sep + 'LICENSE'):
    cmd = ["git", "clone", zippy_megaguards_repo]
    execute_shell(cmd, verbose=True)
    cmd = ['git', 'checkout', zippy_megaguards_branch]
    execute_shell(cmd, cwd=zippy_megaguards_path)
    print_ok("ZipPy+MegaGuards is cloned")

print_info("Checking environment variables for ZipPy+MegaGuards")
if not os.path.isfile(zippy_megaguards_path + os.sep + 'mx.zippy' + os.sep + 'env'):
    with open(zippy_megaguards_path + os.sep + 'mx.zippy' + os.sep + 'env','w') as fp:
        fp.write("# ZipPy environment variables\n")


zippy_default_env_vars = {
    'JAVA_HOME' : jvmci_path[jdk_found],
    'DEFAULT_VM': 'server',
    'DEFAULT_DYNAMIC_IMPORTS': 'truffle/compiler',
    'ZIPPY_MUST_USE_GRAAL': '1',
    'ZIPPY_HOME': zippy_megaguards_path
}

update_env_file(zippy_megaguards_path + os.sep + 'mx.zippy' + os.sep + 'env', zippy_default_env_vars)

print_ok("ZipPy+MegaGuards environment variables is set")

print_info("Building ZipPy+MegaGuards")
cmd = ['mx', 'build']
retcode = execute_shell(cmd, cwd=zippy_megaguards_path, check_only=True, verbose=True)
if retcode != 0:
    print_warn('Previous build was not successful return code: %d' % retcode)
    cmd = ['mx', 'clean']
    execute_shell(cmd, cwd=zippy_megaguards_path)
    cmd = ['mx', 'build']
    execute_shell(cmd, cwd=zippy_megaguards_path, verbose=True)

print_ok("ZipPy+MegaGuards build succeeded")

print_info("Initializing MegaGuards")
cmd = ['mx', 'mg', '--init']
execute_shell(cmd, cwd=zippy_megaguards_path, directly=True)

print_info("Detecting OpenCL devices")
cmd = ['mx', 'mg', '--clinfo']
execute_shell(cmd, cwd=zippy_megaguards_path, directly=True)

print_info("Running unit test for MegaGuards")
cmd = ['mx', 'junit-mg']
retcode = execute_shell(cmd, cwd=zippy_megaguards_path, check_only=True, directly=True)
if retcode != 0:
    print_error('Unit test failed')
    end_script()
else:
    print_ok('Unit test succeeded')

print_ok("Everything looks good")

how_to = """How to use MegaGuards with a Python program?
Use one of these options:
--mg-target=truffle  to execute using a guardless Truffle AST on Graal.
--mg-target=gpu      to execute on a GPU OpenCL device.
--mg-target=cpu      to execute on a CPU OpenCL device.
--mg-target          to execute using our adaptive OpenCL device selection.

for example:
    $ cd """ + zippy_megaguards_name + """
    $ mx python <file.py> --mg-target=gpu
"""
print_info(how_to)

if len(reminders) > 0:
    print_info('Last things:')
    for r in reminders:
        print_info(r)
