@echo off
echo Building the project...
make
echo Build successful

:choice
set /P c=Do you want to run the test cases[Y/N]?
if /I "%c%" EQU "N" goto :exit_from_script
if /I "%c%" EQU "Y" goto :run_test_cases
goto :choice

:exit_from_script
exit

:run_test_cases
cd %~dp0\rpal_test_programs
echo Generating correct outputs for test cases...
call generate_outputs.bat

cd %~dp0\
echo Generating outputs for testing...
java rpal20 rpal_test_programs/rpal_01 > output.01
echo rpal_01 done
java rpal20 rpal_test_programs/rpal_02 > output.02
echo rpal_02 done
java rpal20 rpal_test_programs/rpal_03 > output.03
echo rpal_03 done
java rpal20 rpal_test_programs/rpal_04 > output.04
echo rpal_04 done
java rpal20 rpal_test_programs/rpal_05 > output.05
echo rpal_05 done
java rpal20 rpal_test_programs/rpal_06 > output.06
echo rpal_06 done
java rpal20 rpal_test_programs/rpal_07 > output.07
echo rpal_07 done
java rpal20 rpal_test_programs/rpal_08 > output.08
echo rpal_08 done
java rpal20 rpal_test_programs/rpal_09 > output.09
echo rpal_09 done
java rpal20 rpal_test_programs/rpal_10 > output.10
echo rpal_10 done
java rpal20 rpal_test_programs/rpal_11 > output.11
echo rpal_11 done
java rpal20 rpal_test_programs/rpal_12 > output.12
echo rpal_12 done
java rpal20 rpal_test_programs/rpal_13 > output.13
echo rpal_13 done
java rpal20 rpal_test_programs/rpal_14 > output.14
echo rpal_14 done
java rpal20 rpal_test_programs/rpal_15 > output.15
echo rpal_15 done
java rpal20 rpal_test_programs/rpal_16 > output.16
echo rpal_16 done
java rpal20 rpal_test_programs/rpal_17 > output.17
echo rpal_17 done
java rpal20 rpal_test_programs/rpal_18 > output.18
echo rpal_18 done
java rpal20 rpal_test_programs/rpal_19 > output.19
echo rpal_19 done
java rpal20 rpal_test_programs/rpal_20 > output.20
echo rpal_20 done
java rpal20 rpal_test_programs/rpal_21 > output.21
echo rpal_21 done

echo Getting differences...
echo differences for rpal_01
diff output.01 rpal_test_programs/output01.test
echo differences for rpal_02
diff output.02 rpal_test_programs/output02.test
echo differences for rpal_03
diff output.03 rpal_test_programs/output03.test
echo differences for rpal_04
diff output.04 rpal_test_programs/output04.test
echo differences for rpal_05
diff output.05 rpal_test_programs/output05.test
echo differences for rpal_06
diff output.06 rpal_test_programs/output06.test
echo differences for rpal_07
diff output.07 rpal_test_programs/output07.test
echo differences for rpal_08
diff output.08 rpal_test_programs/output08.test
echo differences for rpal_09
diff output.09 rpal_test_programs/output09.test
echo differences for rpal_10
diff output.10 rpal_test_programs/output10.test
echo differences for rpal_11
diff output.11 rpal_test_programs/output11.test
echo differences for rpal_12
diff output.12 rpal_test_programs/output12.test
echo differences for rpal_13
diff output.13 rpal_test_programs/output13.test
echo differences for rpal_14
diff output.14 rpal_test_programs/output14.test
echo differences for rpal_15
diff output.15 rpal_test_programs/output15.test
echo differences for rpal_16
diff output.16 rpal_test_programs/output16.test
echo differences for rpal_17
diff output.17 rpal_test_programs/output17.test
echo differences for rpal_18
diff output.18 rpal_test_programs/output18.test
echo differences for rpal_19
diff output.19 rpal_test_programs/output19.test
echo differences for rpal_20
diff output.20 rpal_test_programs/output20.test
echo differences for rpal_21
diff output.21 rpal_test_programs/output21.test
pause

cd %~dp0\rpal_test_programs
echo Deleting the generated .test files from rpal_test_programs
del /S /Q %~dp0\rpal_test_programs\*.test
echo Deleting generated output files
del /S /Q %~dp0\output.01
del /S /Q %~dp0\output.02
del /S /Q %~dp0\output.03
del /S /Q %~dp0\output.04
del /S /Q %~dp0\output.05
del /S /Q %~dp0\output.06
del /S /Q %~dp0\output.07
del /S /Q %~dp0\output.08
del /S /Q %~dp0\output.09
del /S /Q %~dp0\output.10
del /S /Q %~dp0\output.11
del /S /Q %~dp0\output.12
del /S /Q %~dp0\output.13
del /S /Q %~dp0\output.14
del /S /Q %~dp0\output.15
del /S /Q %~dp0\output.16
del /S /Q %~dp0\output.17
del /S /Q %~dp0\output.18
del /S /Q %~dp0\output.19
del /S /Q %~dp0\output.20
del /S /Q %~dp0\output.21

echo Starting cleaning up build files...
cd %~dp0\com\proglangproj\group50
echo Deleting the generated .class files from com\proglangproj\group50\*
del /S /Q %~dp0\com\proglangproj\group50\*.class
echo Deleting rpal20.class
del /S /Q %~dp0\rpal20.class

echo Finished cleaning up
exit
