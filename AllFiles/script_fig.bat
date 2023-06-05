echo "This script must be run in folder sources_and_manifest"
echo "It creates the images"
echo "corresponding to figures 11, 12, 14 of the paper"

java orthopaths.CommandCheck < figure11_a.dat
java orthopaths.CommandCheck < figure11_b.dat
java orthopaths.CommandCheck < figure11_c.dat

java orthopaths.CommandCheck < figure12_a.dat
java orthopaths.CommandCheck < figure12_b.dat
java orthopaths.CommandCheck < figure12_c.dat
java orthopaths.CommandCheck < figure12_d.dat

java orthopaths.CommandCheck < figure14_a.dat
java orthopaths.CommandCheck < figure14_b.dat
java orthopaths.CommandCheck < figure14_c.dat
