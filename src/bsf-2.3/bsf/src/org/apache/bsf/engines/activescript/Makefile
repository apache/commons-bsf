#Make file for bsfactivescriptengine

OUTDIR=..\..\..\..\..\..\build\lib\win32\

INTDIR=

!ifdef DEBUG
CPP_OPTIONS=/MDd /Gm /Zi /Od /D _DEBUG /YX
RSC_OPIONS=/d _DEBUG
LINK_OPTIONS=/debug /pdbtype:sept /INCREMENTAL:YES
JNI_INCLUDE=/I$(JAVA_HOME)/include /I$(JAVA_HOME)/include/win32
!else
CPP_OPTIONS=/MD /O2 /D NDEBUG
RSC_OPIONS=/d NDEBUG
LINK_OPTIONS= /INCREMENTAL:NO
JNI_INCLUDE=/I$(JAVA_HOME)/include /I$(JAVA_HOME)/include/win32
!endif

CPP=cl.exe /c
# /D WIN32_LEAN_AND_MEAN /D VC_EXTRALEAN #Leaves out COM stuff
CPP_PROJ=/nologo /W3 /GX /D_WIN32_WINNT=0x400 /DWINVER=0x400\
 $(CPP_OPTIONS)	 /I../../util /D WIN32	/D _WINDOWS /D _WINDLL\
 /D _AFXDLL /D _USRDLL /I . /Fo$(INTDIR) /Fd$(INTDIR) /FD

LINK32=cl.exe

# /NODEFAULTLIB\/IMPLIB:NUL

LINK32_FLAGS= /link /nologo /subsystem:windows /dll \
 /pdb:$(OUTDIR)bsfactivescriptengine.pdb /machine:I386 \
 /out:$(OUTDIR)bsfactivescriptengine.dll $(LINK_OPTIONS) $(LINK32_LIBS)

LINK32_LIBS=
# kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib
# shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib

LINK32_OBJS= \
	$(INTDIR)ActiveScriptEngine.obj \
	$(INTDIR)JavaCOM.obj \
	$(INTDIR)JavaOLEENUMVAR.obj \
	$(INTDIR)JNIUtils.obj

all : $(OUTDIR)bsfactivescriptengine.dll

$(OUTDIR)bsfactivescriptengine.dll : $(OUTDIR) $(DEF_FILE) \
		$(LINK32_OBJS) $(INTDIR)JNIUtils.obj
	$(LINK32) $(LINK32_OBJS) $(LINK32_FLAGS)
	copy $(OUTDIR)bsfactivescriptengine.dll \
             $(OUTDIR)bsfactivescriptengine_g.dll

$(INTDIR)JNIUtils.obj : ..\..\util\JNIUtils.c
	$(CPP) $(CPP_PROJ) $(JNI_INCLUDE) ..\..\util\JNIUtils.c

$(INTDIR)ActiveScriptEngine.obj : ActiveScriptEngine.cpp \
		com_ibm_bsf_engines_activescript_ActiveScriptEngine.h \
		JavaCOM.hpp bsfcom.hpp cashostsite.hpp BSFException.hpp
	$(CPP) $(CPP_PROJ) $(JNI_INCLUDE) $*.cpp

$(INTDIR)JavaCOM.obj : JavaCOM.cpp JavaCOM.hpp BSFException.hpp
	$(CPP) $(CPP_PROJ) $(JNI_INCLUDE) $*.cpp

$(INTDIR)JavaOLEENUMVAR.obj : JavaOLEENUMVAR.cpp JavaOLEENUMVAR.hpp \
		JavaCOM.hpp BSFException.hpp
	$(CPP) $(CPP_PROJ) $(JNI_INCLUDE) $*.cpp

com_ibm_bsf_engines_activescript_ActiveScriptEngine.h :
	javah -classpath $(CLASSPATH);..\..\..\..\..\..\build\lib\bsf.jar \
		-jni com.ibm.bsf.engines.activescript.ActiveScriptEngine

#ENDOF MAKEFILE
