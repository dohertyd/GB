#!/bin/sh

#  golgi.sh
#  GolgiBird
#
#  Created by Brian Kelly on 21/05/2014.
#  Copyright (c) 2014 Openmind Networks. All rights reserved.

PWD=`dirname "$0"`
cp -f ~/Golgi-Pkg/LATEST/iOS/golgi_gen.sh $PWD/.golgi_gen.sh
exec $PWD/.golgi_gen.sh $*




