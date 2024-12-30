package digit.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import digit.service.CollectionFeeService;
import digit.util.ResponseInfoFactory;
import digit.web.models.collectionfee.*;


@RestController
@RequestMapping("/collectionfee")
public class CollectionFeeController {

    @Autowired
    private CollectionFeeService collectionFeeService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping("/_save")
    public ResponseEntity<CollectionFeeResponse> saveCollectionFee(
        @RequestBody CollectionFeeRequest collectionFeeRequest){
            CollectionFee collection = null;
            CollectionFeeResponse response = new CollectionFeeResponse();
            try {
                collection = collectionFeeRequest.getCollectionFee();
                collectionFeeService.saveCollectionFee(collectionFeeRequest);

                response = CollectionFeeResponse.builder()
                          .animalid(collection.getAnimalid())
                          .feetype(collection.getFeetype())
                          .feevalue(collection.getFeevalue())
                          .build();

                // Return the response with HTTP status OK
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                // Log the error and return HTTP INTERNAL SERVER ERROR
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }


        @PostMapping("/slaughterunitfee/_save")
    public ResponseEntity<SlaughterUnitFeeResponse> saveSlaughterUnitFee(
        @RequestBody SlaughterUnitFeeRequest request){
            SlaughterUnitFee slaughterUnitFee = null;
            SlaughterUnitFeeResponse response = new SlaughterUnitFeeResponse();
            try {
                slaughterUnitFee = request.getSlaughterUnitFee();
                collectionFeeService.saveSlaughterUnitFee(request);
                long time = System.currentTimeMillis();
                response = SlaughterUnitFeeResponse.builder()
                          .animaltypeid(slaughterUnitFee.getAnimaltypeid())
                          .charges(slaughterUnitFee.getCharges())
                          .slaughterunitshiftid(slaughterUnitFee.getSlaughterunitshiftid())
                          .createdat(time)
                          .build();

                // Return the response with HTTP status OK
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                // Log the error and return HTTP INTERNAL SERVER ERROR
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
}
