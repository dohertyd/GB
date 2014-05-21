#!/bin/sh

CG_DOT_SH="golgi_gen.sh"
SCRIPT_DIR=`dirname "$0"`
SCRIPT_NAME=`basename "$0"`
#echo "S: $SCRIPT_NAME"
#echo "PWD: `pwd`"
#echo "$0"

if [ "$SCRIPT_NAME" != ".$CG_DOT_SH" ]; then
    cp -f "$0" "$SCRIPT_DIR/.$CG_DOT_SH"
    exec "$SCRIPT_DIR/.$CG_DOT_SH" $*
fi

PKG_DIR=`dirname "$0"`

LATEST_PKG=`ls ~/Golgi-Pkg | sort | tail -1`

LIBS_DONE=0
GJAR=""
NEW_CGEN=""

for d in ~/Dropbox/GOLGI-IOS-LOCAL-BUILD ~/Golgi-Pkg/$LATEST_PKG/common; do
    if [ "$GJAR" = "" -a -f $d/garrick_combined.jar ]; then
        GJAR="$d/garrick_combined.jar"
    fi
done

for d in ~/Dropbox/GOLGI-IOS-LOCAL-BUILD ~/Golgi-Pkg/$LATEST_PKG/iOS; do
    if [ "$CGEN" = "" -a -f $d/$CG_DOT_SH ]; then
        CGEN="$d/$CG_DOT_SH"
    fi
done

if [ "$CGEN" = "" ]; then
    echo "Zoikes, cannot locate $CG_DOT_SH"
exit -1
fi
M1=`cat $CGEN | md5`
M2=`cat "$0" | md5`

if [ "$M1" != "$M2" ]; then
    echo "Clobbering $CG_DOT_SH and re-running"
    cp -f "$CGEN" "$SCRIPT_DIR/"
    exec "$SCRIPT_DIR/$CG_DOT_SH" $*
fi


TGT="$PROJECT_DIR/$PROJECT_NAME"

for d in ~/Dropbox/GOLGI-IOS-LOCAL-BUILD ~/Golgi-Pkg/$LATEST_PKG/iOS; do
    if [ $LIBS_DONE -eq 0 -a -d $d ]; then
        LIBS_DONE=1
        for f in libGolgi.a libGolgi.h libGolgiLite.a; do
            CP=0
            if [ ! -f "$TGT/$f" ]; then
                CP=1
            else
                M1=`cat "$TGT/$f" | md5`
                M2=`cat "$d/$f" | md5`
                if [ "$M1" != "$M2" ]; then
                    CP=1
                fi
            fi
            if [ $CP -ne 0 ]; then
                echo "REPLACING $TGT/$f"
                cp -f "$d/$f" "$TGT/$f"
                if [ $? -ne 0 ]; then
                    exit -1
                fi
            fi
        done
    fi
done


if [ "$GJAR" = "" ]; then
    echo "Zoikes, cannot locate JAR file for code generation"
    exit -1
fi

FILES=`find . -name '*.thrift' -print`

if [ "$FILES" = "" ]; then
    echo "No thrift files in directory"
    exit -1
fi

ODIR=.

find . -name '*.thrift' -print | (
    while read TF; do
        SVCDIR=`dirname "$TF"`
        SVC=`basename "$TF" | sed -e 's/\.thrift//'`
        HFILE="$SVC""SvcGen.h"
        MFILE="$SVC""SvcGen.m"
        (
            cd "$SVCDIR";
            rm -f "$HFILE" "$MFILE"

            if [ "$1" != "clean" ]; then
                echo "  TF: '$TF'"
                /bin/echo -n "Generating Code for $SVC: "
                java -classpath $GJAR  com.openmindnetworks.golgi.garrick.Garrick -i `basename "$TF"` -ocdir . -ocdh "$HFILE" -ocdm "$MFILE"
                rc=$?
                if [ $rc -ne 0 ]; then
                    echo "Code Generation For $TF Failed: $rc"
                    exit -1
                fi
                echo "Done "
            fi
        )
        rc=$?
        if [ $rc -ne 0 ]; then
            exit -1
        fi
    done
)

exit 0
