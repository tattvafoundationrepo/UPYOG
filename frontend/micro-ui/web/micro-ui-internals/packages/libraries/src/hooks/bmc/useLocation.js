import { useQuery } from 'react-query';
import { LocationService } from "../../services/elements/Location";
import SchemeService from '../../services/elements/Scheme';

const useLocation = (tenantId, locationType, config = {}) => {
    switch(locationType) {
        case 'Locality':
            return useQuery(["LOCALITY_DETAILS", tenantId ], () => LocationService.getLocalities(tenantId), config);   
        case 'Ward':
            return useQuery(["WARD_DETAILS", tenantId ], () => LocationService.getWards(tenantId), config);
        case 'Block':
            return useQuery(["BLOCK_DETAILS", tenantId ], () => LocationService.getBlocks(tenantId), config);
        case 'Zone':
            return useQuery(["ZONE_DETAILS", tenantId ], () => LocationService.getZones(tenantId), config);
        default:
            return useQuery(["PINCODE_DETAILS", tenantId ], () => SchemeService.getPincode(tenantId), config);
    } 
}

export default useLocation;
