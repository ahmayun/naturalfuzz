
echo 'ahmad soft nofile 1048576' >> /etc/security/limits.conf
echo 'ahmad hard nofile 1048576' >> /etc/security/limits.conf
echo 'fs.file-max = 1048576' >> /etc/sysctl.conf
sysctl -p