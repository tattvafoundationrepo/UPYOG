import { useMutation } from "react-query";
import EBEService from "../../services/elements/EBE";

export const useEBECreateEnquiry = (config = {}) => {
  return useMutation((data) => EBEService.createEnquiry(data), config);
};

export default useEBECreateEnquiry;