FILES=./*.wav
for f in $FILES
do
	echo "processing $f ..."
	g=$(basename $f .wav)

	#echo $g
	mv $f $g\_gr_paige.wav
done
