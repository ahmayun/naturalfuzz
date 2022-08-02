sudo yum install java-devel
curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs
chmod +x cs
./cs setup
rm cs
cs update cs
cs install scala:2.11.8 && cs install scalac:2.11.8