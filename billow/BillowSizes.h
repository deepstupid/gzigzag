// k‰sitell‰‰n aina erikseen n‰m‰ koot, t‰m‰ on se olio jonka kanssa
// k‰ytt‰j‰ vuorovaikuttaa.
class BillowSizes {
 public:
        virtual double getSize(int line, double &y) = 0;
        virtual int getLine(double y) = 0; // Get line index at y-coordinate
        // If we need the x index, we re-render (or virtually so)
	virtual ~BillowSizes() {}
};
