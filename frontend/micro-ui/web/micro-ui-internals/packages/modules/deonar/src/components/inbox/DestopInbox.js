import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import InboxLinks from "../inbox/ApplicationLinks";
import CustomTable from "../../pages/employee/commonFormFields/customTable";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import jsonData from './dummyInboxData.json';
import InteractiveCharts from "../../pages/employee/commonFormFields/InteractiveCharts";

const DesktopInbox = ({ tableConfig, filterComponent, ...props }) => {
  const { t } = useTranslation();
  const tenantIds = Digit.SessionStorage.get("BMC_TENANTS");
  const [totalRecords, setTotalRecords] = useState(0);

  const [FilterComponent, setComp] = useState(() => Digit.ComponentRegistryService?.getComponent(filterComponent));

  const { fetchEntryFeeDetailsbyUUID } = useDeonarCommon();

  const { data: fetchedData, isLoading } = fetchEntryFeeDetailsbyUUID({ forCollection: true });
  const [animalCount, setAnimalCount] = useState([]);

  const baseColumns = [
    {
      Header: "ID",
      accessor: "id",
      Cell: ({ row }) => row.index + 1,
      isVisible: false,
    },
    {
      Header: "DEONAR_ARRIVAL_UUID",
      accessor: "entryUnitId",
      // Cell: ({ row }) => (
      //   <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
      //     {row.original.entryUnitId}
      //   </span>
      // ),
    },
    {
      Header: "DEONAR_TRADER_NAME",
      accessor: "traderName",
    },
    {
      Header: "DEONAR_LICENSE_NUMBER",
      accessor: "licenceNumber",
    },
    {
      Header: "ARRIVAL_DATE_FIELD",
      accessor: "dateOfArrival",
    },
    {
      Header: "ARRIVAL_TIME_FIELD",
      accessor: "timeOfArrival",
    },
  ];

  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails || []);
      setTotalRecords();
    }
  }, [fetchedData]);

  const transformedAnimalCount = animalCount.map((item) => ({
    entryUnitId: item.entryUnitId,
    traderName: item.traderName,
    licenceNumber: item.licenceNumber,
    dateOfArrival: item.dateOfArrival,
    timeOfArrival: item.timeOfArrival,
  }));


  function filterDistinctServiceData(data, keys, type, filterValues) {
    // Step 1: Filter the data based on filter values (day, month, year)
    let filteredData = data;
  
    // Destructure to get day, month, and year
    const { day, month, year } = filterValues;
  
    if (type === 'daily') {
      // Filter by day, month, and year
      filteredData = filteredData.filter(entry => {
        return entry.day === day &&  // day
               entry.month === month && // month
               entry.year === year;    // year
      });
    } else if (type === 'monthly') {
      // Filter by month and year
      filteredData = filteredData.filter(entry => 
        entry.month === month &&  // month
        entry.year === year);    // year
    } else if (type === 'yearly') {
      // Filter by year
      filteredData = filteredData.filter(entry => 
        entry.year === year);    // year
    }
    // Step 2: Process the filtered data to get distinct values
    const seen = new Set();
    const result = [];
  
    filteredData.forEach(entry => {
      let uniqueKey;
      
      // Determine the unique key based on the type
      if (type === 'daily') {
        uniqueKey = `${entry.name}_${entry.servicedailyearning}_${entry.day}_${entry.month}_${entry.year}`;  // name, servicedailyearning, day, month, year
      } else if (type === 'monthly') {
        uniqueKey = `${entry.name}_${entry.servicedailyearning}_${entry.month}_${entry.year}`;  // name, servicedailyearning, month, year
      } else if (type === 'yearly') {
        uniqueKey = `${entry.name}_${entry.servicedailyearning}_${entry.year}`;  // name, servicedailyearning, year
      }
  
      // If the unique key has not been seen before, add the entry to the result
      if (!seen.has(uniqueKey)) {
        result.push(keys.reduce((obj, key) => {
          obj[key] = entry[key];
          return obj;
        }, {}));
        
        // Mark the combination as seen
        seen.add(uniqueKey);
      }
    });
  
    return { data: result };
  }

  function generateChartData(filteredData, labelKey, dataKey) {
    // Step 1: Prepare Pie Chart Data (labels and data)
    const labels = [];
    const data = [];
    const backgroundColor = [];
    const hoverBackgroundColor = [];
    
    // Step 2: Collect unique labels and their corresponding data
    filteredData.data.forEach(entry => {
      const label = entry[labelKey]; // label is taken from the `labelKey` provided
      const dataValue = entry[dataKey]; // data is taken from the `dataKey` provided
  
      // Debugging: Check if label or dataValue is null or undefined
      if (label === null || label === undefined) {
        console.warn('Null label found for entry:', entry);
      }
      
      if (dataValue === null || dataValue === undefined) {
        console.warn('Null data found for entry:', entry);
      }
      
      // Add the label and corresponding data if not already added
      if (label && !labels.includes(label)) {
        labels.push(label);
        data.push(dataValue);
        
        // Generate colors based on the number of labels
        const color = generateRandomColor(); // Generate random color
        backgroundColor.push(color);
        hoverBackgroundColor.push(color);
      }
    });
  
    // Debugging: Check final labels and data arrays
    console.log('Labels:', labels);
    console.log('Data:', data);
    
    // Step 3: Prepare the Details Data (per label)
    const detailsData = {};
  
    labels.forEach(label => {
      // Filter the data for this specific label
      const labelData = filteredData.data.filter(entry => entry[labelKey] === label);
  
      // Generate the details for each label
      const detailsLabels = [];
      const detailsValues = [];
  
      labelData.forEach(entry => {
        // Collect individual `day` values or other time-specific data for each entry
        detailsLabels.push(entry.day); // Or any other time-specific key
        detailsValues.push(entry[dataKey]);
      });
  
      detailsData[label] = {
        labels: detailsLabels, // Day-wise or time-specific labels
        data: detailsValues,   // Corresponding values for each time period
      };
    });
  
    // Step 4: Prepare the Pie Chart Data
    const pieChartData = {
      labels: labels,
      datasets: [
        {
          label: 'Services',
          data: data,
          backgroundColor: backgroundColor,
          hoverBackgroundColor: hoverBackgroundColor,
        },
      ],
    };
  
    return { pieChartData, detailsData };
  }
  
  // Helper function to generate a random color for each label
  function generateRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  }

const dailyFilteredData = filterDistinctServiceData(jsonData.eg_deonar_rptviewearning, ["name", "servicedailyearning", "day"], 'daily',{ day: 13, month: 12, year: 2024 });
const monthlyFilteredData = filterDistinctServiceData(jsonData.eg_deonar_rptviewearning, ["name", "servicemonthlyearning", "month", "year"], 'monthly',{ month: 12, year: 2024 });
const yearlyFilteredData = filterDistinctServiceData(jsonData.eg_deonar_rptviewearning, ["name", "serviceyearlyearning", "year"], 'yearly',{ year: 2024 });

  console.log("dailyFilteredData",dailyFilteredData);
  console.log("monthlyFilteredData",monthlyFilteredData);
  console.log("yearlyFilteredData",yearlyFilteredData);
  

const labelKey = 'name'; // Use the "name" field as the label
const dataKey = 'servicedailyearning'; // Use the "servicedailyearning" field as the data value

const { pieChartData, detailsData } = generateChartData(dailyFilteredData, labelKey, dataKey);

console.log('Pie Chart Data:', pieChartData);
console.log('Details Data:', detailsData);

  return (
    <div className="inbox-container" style={{ overflow: "auto", scrollbarWidth: "none", msOverflowStyle: "none" }}>
      {!props.isSearch && (
        <div className="filters-container">
          <InboxLinks
            parentRoute={props.parentRoute}
            allLinks={[
              {
                text: "BMC_MASTER",
                link: `/digit-ui/employee/deonar/create`,
                businessService: "deonar",
                roles: ["SUPERUSER"],
              },
            ]}
            headerText={"DEONAR"}
            businessService={props.businessService}
          />
          <div>
            {
              <FilterComponent
                defaultSearchParams={props.defaultSearchParams}
                onFilterChange={props.onFilterChange}
                searchParams={props.searchParams}
                type="desktop"
                tenantIds={tenantIds}
              />
            }
          </div>
        </div>
      )}
      {
        <div className="result" style={{ marginLeft: !props?.isSearch ? "24px" : "", flex: 1 }}>
          <CustomTable
            t={t}
            columns={baseColumns}
            data={transformedAnimalCount}
            manualPagination={false}
            totalRecords={totalRecords}
            sortParams={[]}
            config={[]}
            tableClassName={"ebe-custom-scroll"}
            isLoadingRows={isLoading}
            showSearch={true}
            showText={true}
            showPdfDownload={true}
            showExcelDownload={true}
            pdfButtonText="Download PDF"
            excelButtonText="Download Excel"
            customDownloadButtonStyles={{
              fontSize: "14px",
              fontWeight: "bold",
            }}
          />
          <InteractiveCharts pieChartData={pieChartData} detailsData={detailsData}></InteractiveCharts>
        </div>
      }
    </div>
  );
};

export default DesktopInbox;
