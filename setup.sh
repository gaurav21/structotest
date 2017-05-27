#!/bin/bash
plcmodel=$1
basedir=$(dirname "$0")
cd $basedir
if [ -d '/home/structo/bin/v1.3.8' ];
then
exit 1
fi
mv /tmp/v1.3.8 /home/structo/bin/v1.3.8
pkexec bash /tmp/installfiles.sh
if  [ ! -f /home/structo/TestSerial.py ]; then
cp -R /tmp/applicationfiles/TestSerial.py /home/structo/
cp -R /tmp/applicationfiles/start.sh /home/structo/
cp -R /tmp/applicationfiles/start_updater.sh /home/structo/
chmod +x /home/structo/start_updater.sh
fi
cp -R /tmp/applicationfiles/quickReleaseMask.png /home/structo/.structo/
cp -R /tmp/applicationfiles/comm.json /home/structo/.structo/
cp -R /tmp/applicationfiles/mount.sh /home/structo/
cd /home/structo/bin/v1.3.8
if md5sum -c checksum.txt ; then
rm /home/structo/bin/current
ln -s /home/structo/bin/v1.3.8 /home/structo/bin/current
echo "v1.3.8" > /home/structo/.structo/version.txt
echo "setup complete"
else
rm -rf /home/structo/bin/v1.3.8
echo setup failed
fi
