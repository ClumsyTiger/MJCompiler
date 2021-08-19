# ________________________________________________________________________________________________________________
# Manual

# 1) Add Ant to the environment variable 'path' (if it isn't already there):
#    +   .../NetBeans/netbeans/extide/ant/bin
# 2) Enjoy!



# ________________________________________________________________________________________________________________
# Reference materials

# https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-hashtable?view=powershell-7.1
# https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-arrays?view=powershell-7.1
# https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-hashtable?view=powershell-7.1#saving-a-nested-hashtable-to-file
# https://docs.microsoft.com/en-us/powershell/scripting/learn/ps101/06-flow-control?view=powershell-7.1
# https://docs.microsoft.com/en-us/powershell/scripting/learn/ps101/09-functions?view=powershell-7.1

# https://docs.microsoft.com/en-us/dotnet/api/system.collections.arraylist?view=net-5.0
# https://powershellexplained.com/2017-11-20-Powershell-StringBuilder/
# https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-null?view=powershell-7.1
# https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.management/remove-item?view=powershell-7.1#example-4--delete-files-in-subfolders-recursively
# https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-switch?view=powershell-7.1
# https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-hashtable?view=powershell-7.1#splatting-hashtables-at-cmdlets
# https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_operators?view=powershell-7.1



# ________________________________________________________________________________________________________________
# Functions

# find all files with the given extension recursively relative to the current working directory
function Find-Recursive
{
    # this turns a regular function into an advanced function
    [CmdletBinding()]
    # function parameters
    param( [string] $Extension )
    
    # always initialize variables before use (since an exception can occur during assignment)
    $Files = $null;
    # find files with the given extension recursively in the current working directory
    $Files = Get-ChildItem -Path "$PWD" -Include "*$Extension" -File -Recurse;

    # for all files, get the full name from the file info, get the path relative to the current working directory, and finally convert the result to a string
    # +   return an array of such files
    if( $Files ) { @(   $Files.FullName | Resolve-Path -Relative | ForEach-Object -Process { $_.ToString() }   ); }
    else         { @(); }
}



# ________________________________________________________________________________________________________________
# Build script

if( "-help" -in $args -or "--help" -in $args )
{
    Write-Output "build   [[-]-help]   [-jflex] [-cup [-ast] [-pst]]   [-clean] [-build]   [-compile [-verbose] [-lex file] [-par file] [-o file] file]   [-disasm file] [-run -debug file]";
    Write-Output "";
    Write-Output "Default:        build -jflex -ast -cup -build";
    Write-Output "";
    Write-Output "Switches:";
    Write-Output "    --help      shows the help menu";
    Write-Output "    -help       same as --help";
    Write-Output "";
    Write-Output "    -jflex      runs the JFlex tool on the flex specification";
    Write-Output "    -cup        runs the CUP tool on the cup specification";
    Write-Output "      -ast      +   generates the abstract syntax tree using the cup specification";
    Write-Output "      -pst      +   shows the generated parser states";
    Write-Output "";
    Write-Output "    -clean      cleans the project (except source files generated by the jflex and cup tools)";
    Write-Output "    -build      builds the project";
    Write-Output "";
    Write-Output "    -compile    compiles the MJ program with the given parameters";
    Write-Output "      -verbose  +   enable verbose compiler output";
    Write-Output "      -lex      +   generate the lexer output and write to the <output fname>.lex";
    Write-Output "      -par      +   generate the parser output and write to the <output fname>.par";
    Write-Output "      -o        +   specify the name of the output file (<input fname>.obj by default)";
    Write-Output "";
    Write-Output "    -disasm     disassembles the .obj file";
    Write-Output "    -run        runs the .obj file on the MicroJava virtual machine";
    Write-Output "      -debug    +   run in debug mode";
    Write-Output "";
    exit 0;
}


# default build script arguments if none specified
if( $args.count -eq 0 )
{
    # leave powershell array constructor ( @() ) even if there is only one argument (otherwise it won't be a powershell array due to unpacking)
    $args = @( "-jflex", "-ast", "-cup", "-build" );
}

# calculate the "MJ compiler", "disassembler" and "run" commands' positions in the argument list
# +   [-compile [-lex] [-par] [-cmp] [-o file] file]   [-disasm file]   [-run file]
#       beg    --------------------------------------    end
#                                                        beg   --------   end
#                                                                         beg   -   end
$CompileArgs = $null;
$DisasmArgs = $null;
$RunArgs = $null;

if( $true )
{
    $CompileArgs_Beg, $CompileArgs_End = [array]::indexof( $args, "-compile" ), $args.count;
    $DisasmArgs_Beg,  $DisasmArgs_End  = [array]::indexof( $args, "-disasm" ),  $args.count;
    $RunArgs_Beg,     $RunArgs_End     = [array]::indexof( $args, "-run" ),     $args.count;

    if( $CompileArgs_Beg -ge 0 )
    {
        if    ( $DisasmArgs_Beg -ge 0 ) { $CompileArgs_End = $DisasmArgs_Beg; }
        elseif( $RunArgs_Beg    -ge 0 ) { $CompileArgs_End = $RunArgs_Beg; }

        if( $CompileArgs_Beg + 1   -le   $CompileArgs_End - 1 ) { $CompileArgs = $args[ ($CompileArgs_Beg + 1) .. ($CompileArgs_End - 1) ]; }
    }
    if( $DisasmArgs_Beg -ge 0 )
    {
        if( $RunArgs_Beg -ge 0 ) { $DisasmArgs_End = $RunArgs_Beg; }

        if( $DisasmArgs_Beg + 1   -le   $DisasmArgs_End - 1 ) { $DisasmArgs = $args[ ($DisasmArgs_Beg + 1) .. ($DisasmArgs_End - 1) ]; }
    }
    if( $RunArgs_Beg -ge 0 )
    {
        if( $RunArgs_Beg + 1   -le   $RunArgs_End - 1 ) { $RunArgs = $args[ ($RunArgs_Beg + 1) .. ($RunArgs_End - 1) ]; }
    }
}



# cup tool
if( "-cup" -in $args )
{
    Write-Output "---------------------------------------------------------------------------------------------------------------- <<< CUP";

    # always initialize variables before use (since an exception can occur during assignment)
    $BuildCmd = $null;
    
    # create the build comand array
    # +   relative paths should be relative to the project 'src' folder
    $BuildCmd = "java",
        "-cp", "'../lib/cup_v10k.jar'", "java_cup.Main",
        "-destdir", "'./rs/ac/bg/etf/pp1'",
        "-parser", "'Parser'",
        "-interface", "-symbols", "'ISymbolCode'";

    # build the abstract syntax tree
    if( "-ast" -in $args )
    {
        $BuildCmd += "-ast", "rs.ac.bg.etf.pp1.ast", "-buildtree";

        # remove previously generated ast code
        if( Test-Path "./MJCompiler/src/rs/ac/bg/etf/pp1/ast" -PathType "Container" )
        {
            Remove-Item "./MJCompiler/src/rs/ac/bg/etf/pp1/ast" -Recurse;
        }
    }

    # show parser states
    if( "-pst" -in $args )
    {
        $BuildCmd += "-dump_states";
    }

    # set the specification file location
    $BuildCmd += "'../spec/mjparser.cup'";

    # join the array of strings into a single string separated by spaces
    $BuildCmd = $BuildCmd -Join ' ';

    # print the build command
    Write-Output $BuildCmd;


    # join the array of strings into a single string separated by spaces
    $BuildCmd = $BuildCmd -Join ' ';

    # set current working directory to the project 'src' folder, since the ast_cup tool can't find the 'ast' folder otherwise
    Push-Location "./MJCompiler/src";

    # invoke the build command
    Invoke-Expression -Command $BuildCmd;

    # restore the previous working directory
    Pop-Location;

    # exit with the error code of the last native program that was run
    if( $LASTEXITCODE -ne 0 ) { exit $LASTEXITCODE; }
}


# jflex tool
# +   jflex should be run after cup, since its output depends on cup output (more precisely on the cup-generated symbol class)
if( "-jflex" -in $args )
{
    Write-Output "---------------------------------------------------------------------------------------------------------------- <<< JFlex";

    # always initialize variables before use (since an exception can occur during assignment)
    $BuildCmd = $null;

    # create the build comand array
    $BuildCmd = "java",
        "-cp", "'./MJCompiler/lib/jflex-1.4.3.jar'", "JFlex.Main",
        "-nobak",
        "-d", "'./MJCompiler/src/rs/ac/bg/etf/pp1'",
        "'./MJCompiler/spec/mjlexer.flex'";

    # join the array of strings into a single string separated by spaces
    $BuildCmd = $BuildCmd -Join ' ';

    # print the build command
    Write-Output $BuildCmd;

    # invoke the build command
    Invoke-Expression -Command $BuildCmd;

    # exit with the error code of the last native program that was run
    if( $LASTEXITCODE -ne 0 ) { exit $LASTEXITCODE; }
}


# build project
if( "-clean" -in $args -or "-build" -in $args )
{
    Write-Output "---------------------------------------------------------------------------------------------------------------- <<< BUILD";

    if( "-clean" -in $args )
    {
        # remove compiled java code directories
        if( Test-Path "./MJCompiler/bin"   -PathType "Container" ) { Remove-Item "./MJCompiler/bin"   -Recurse; }
        if( Test-Path "./MJCompiler/build" -PathType "Container" ) { Remove-Item "./MJCompiler/build" -Recurse; }
        if( Test-Path "./MJCompiler/dist"  -PathType "Container" ) { Remove-Item "./MJCompiler/dist"  -Recurse; }

        # remove 'logs' directory
        if( Test-Path "./MJCompiler/logs" -PathType "Container" ) { Remove-Item "./MJCompiler/logs" -Recurse; }
        if( Test-Path "./MJCompiler/test/logs" -PathType "Container" ) { Remove-Item "./MJCompiler/test/logs" -Recurse; }

        # remove the generated cup specification files from the 'spec' directory
        Get-ChildItem -Path "./MJCompiler/spec" -Include "*_astbuild.cup" -File -Recurse | Remove-Item;

        # remove all .lex, .par and .obj files from the test directory
        # +   When it is used with the Include parameter, the Recurse parameter might not delete all subfolders or all child items. This is a known issue.
        # +   As a workaround, try piping results of the Get-ChildItem -Recurse command to Remove-Item, as described in "Example 4" in this topic.
        Get-ChildItem -Path "./MJCompiler/test" -Include "*.lex" -File -Recurse | Remove-Item;
        Get-ChildItem -Path "./MJCompiler/test" -Include "*.par" -File -Recurse | Remove-Item;
        Get-ChildItem -Path "./MJCompiler/test" -Include "*.obj" -File -Recurse | Remove-Item;
    }


    if( "-clean" -in $args -and "-build" -in $args )
    {
        $Action = "rebuild clean jar";
    }
    elseif( "-build" -in $args )
    {
        $Action = "build jar";
    }
    elseif( "-clean" -in $args )
    {
        $Action = "clean";
    }

    # always initialize variables before use (since an exception can occur during assignment)
    $BuildCmd = $null;
    # create the build comand array
    $BuildCmd = "ant",
        "-quiet",
        "-f", "./MJCompiler",
        "-Dnb.internal.action.name='$Action'";

    # join the array of strings into a single string separated by spaces
    $BuildCmd = $BuildCmd -Join ' ';

    # print the build command
    Write-Output $BuildCmd;


    # clean output from unnecessary info
    $BuildCmd += "|",
        "Select-String", "-NotMatch", "-Pattern", "'\[javadoc\]'";

    # join the array of strings into a single string separated by spaces
    $BuildCmd = $BuildCmd -Join ' ';

    # invoke the build command
    Invoke-Expression -Command $BuildCmd;

    # exit with the error code of the last native program that was run
    if( $LASTEXITCODE -ne 0 ) { exit $LASTEXITCODE; }
}



# compile the given MicroJava source code
if( "-compile" -in $args )
{
    Write-Output "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER";

    # always initialize variables before use (since an exception can occur during assignment)
    $RunCmd = $null;
    # create the run comand array
    $RunCmd = "java",
        "-cp", "'../dist/MJCompiler.jar'", "rs.ac.bg.etf.pp1.Compiler";

    if( $CompileArgs.count -gt 0 ) { $RunCmd += $CompileArgs; }

    # join the array of strings into a single string separated by spaces
    $RunCmd = $RunCmd -Join ' ';

    # print the run command
    Write-Output $RunCmd;

    # set current working directory to the project 'test' folder to simplify test file paths
    Push-Location "./MJCompiler/test";

    # invoke the run command
    Invoke-Expression -Command $RunCmd;

    # restore the previous working directory
    Pop-Location;

    # exit with the error code of the last native program that was run
    if( $LASTEXITCODE -ne 0 ) { exit $LASTEXITCODE; }
}



# disassemble the given MicroJava .obj file
if( "-disasm" -in $args )
{
    Write-Output "---------------------------------------------------------------------------------------------------------------- <<< MJ DISASM";

    # always initialize variables before use (since an exception can occur during assignment)
    $RunCmd = $null;
    # create the run comand array
    $RunCmd = "java",
        "-cp", "'../lib/mj-runtime-1.1.jar'", "rs.etf.pp1.mj.runtime.disasm";

    if( $DisasmArgs.count -gt 0 ) { $RunCmd += $DisasmArgs; }

    # join the array of strings into a single string separated by spaces
    $RunCmd = $RunCmd -Join ' ';

    # print the run command
    Write-Output $RunCmd;

    # set current working directory to the project 'test' folder to simplify test file paths
    Push-Location "./MJCompiler/test";

    # invoke the run command
    Invoke-Expression -Command $RunCmd;

    # exit with the error code of the last native program that was run
    if( $LASTEXITCODE -ne 0 ) { exit $LASTEXITCODE; }

    # restore the previous working directory
    Pop-Location;
}



# run the given MicroJava compiled code
if( "-run" -in $args )
{
    Write-Output "---------------------------------------------------------------------------------------------------------------- <<< MJ VM";

    # always initialize variables before use (since an exception can occur during assignment)
    $RunCmd = $null;
    # create the run comand array
    $RunCmd = "java",
        "-cp", "'../lib/mj-runtime-1.1.jar'", "rs.etf.pp1.mj.runtime.Run";

    if( $RunArgs.count -gt 0 ) { $RunCmd += $RunArgs; }

    # join the array of strings into a single string separated by spaces
    $RunCmd = $RunCmd -Join ' ';

    # print the run command
    Write-Output $RunCmd;

    # set current working directory to the project 'test' folder to simplify test file paths
    Push-Location "./MJCompiler/test";

    # invoke the run command
    Invoke-Expression -Command $RunCmd;

    # exit with the error code of the last native program that was run
    if( $LASTEXITCODE -ne 0 ) { exit $LASTEXITCODE; }

    # restore the previous working directory
    Pop-Location;
}



# exit success
exit 0;





