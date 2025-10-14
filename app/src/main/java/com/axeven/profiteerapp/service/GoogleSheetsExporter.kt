package com.axeven.profiteerapp.service

import com.axeven.profiteerapp.utils.logging.Logger
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.AutoResizeDimensionsRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.NumberFormat
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.TextFormat
import com.google.api.services.sheets.v4.model.ValueRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Service for exporting transaction data to Google Sheets.
 *
 * Handles the creation of spreadsheets, writing data, and formatting cells
 * for optimal presentation. Follows TDD approach with comprehensive test coverage.
 *
 * @property logger Logger for tracking export operations
 */
class GoogleSheetsExporter @Inject constructor(
    private val logger: Logger
) {

    companion object {
        private const val DEFAULT_SHEET_NAME_PREFIX = "Transactions Export"
        private const val DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss"
        private const val MAX_ROWS_PER_REQUEST = 1000

        // Column indices for specific data types (0-indexed)
        private const val DATE_COLUMN_INDEX = 0
        private const val AMOUNT_COLUMN_INDEX = 3

        // Header row formatting colors
        private const val HEADER_BACKGROUND_RED = 0.85f
        private const val HEADER_BACKGROUND_GREEN = 0.85f
        private const val HEADER_BACKGROUND_BLUE = 0.85f
    }

    /**
     * Creates a new Google Spreadsheet.
     *
     * @param sheetsService The authenticated Sheets service instance
     * @param sheetName Optional custom name for the spreadsheet. If not provided,
     *                  a default name with timestamp will be used.
     * @return Result containing the created Spreadsheet or exception on failure
     */
    fun createSpreadsheet(
        sheetsService: Sheets,
        sheetName: String = generateDefaultSheetName()
    ): Result<Spreadsheet> {
        return try {
            logger.d("GoogleSheetsExporter", "Creating spreadsheet: $sheetName")

            val spreadsheet = Spreadsheet().apply {
                properties = SpreadsheetProperties().apply {
                    title = sheetName
                }
            }

            val result = sheetsService.spreadsheets()
                .create(spreadsheet)
                .execute()

            logger.i(
                "GoogleSheetsExporter",
                "Spreadsheet created successfully: ${result.spreadsheetId}"
            )
            Result.success(result)

        } catch (e: Exception) {
            logger.e("GoogleSheetsExporter", "Failed to create spreadsheet", e)
            Result.failure(e)
        }
    }

    /**
     * Writes data to a Google Spreadsheet.
     *
     * Handles large datasets by potentially batching requests if needed.
     * Uses APPEND mode to add data to the sheet.
     *
     * @param sheetsService The authenticated Sheets service instance
     * @param spreadsheetId The ID of the spreadsheet to write to
     * @param data List of rows, where each row is a list of cell values
     * @return Result indicating success or failure
     */
    fun writeData(
        sheetsService: Sheets,
        spreadsheetId: String,
        data: List<List<String>>
    ): Result<AppendValuesResponse> {
        return try {
            logger.d(
                "GoogleSheetsExporter",
                "Writing ${data.size} rows to spreadsheet: $spreadsheetId"
            )

            // Convert data to ValueRange format
            val valueRange = ValueRange().apply {
                setValues(data.map { row -> row.toList() })
            }

            // Append data to the first sheet (Sheet1)
            val response = sheetsService.spreadsheets().values()
                .append(spreadsheetId, "Sheet1", valueRange)
                .setValueInputOption("RAW") // Use RAW to preserve data as-is
                .setInsertDataOption("INSERT_ROWS") // Insert new rows
                .execute()

            logger.i(
                "GoogleSheetsExporter",
                "Successfully wrote ${data.size} rows. Updated range: ${response.updates?.updatedRange}"
            )
            Result.success(response)

        } catch (e: Exception) {
            logger.e("GoogleSheetsExporter", "Failed to write data to spreadsheet", e)
            Result.failure(e)
        }
    }

    /**
     * Applies formatting to cells in the spreadsheet.
     *
     * Formatting includes:
     * - Bold header row with light gray background
     * - Currency formatting for amount columns
     * - Date formatting for date columns
     * - Auto-sized column widths
     *
     * @param sheetsService The authenticated Sheets service instance
     * @param spreadsheetId The ID of the spreadsheet to format
     * @param sheetId The ID of the specific sheet within the spreadsheet (typically 0)
     * @return Result indicating success or failure
     */
    fun formatCells(
        sheetsService: Sheets,
        spreadsheetId: String,
        sheetId: Int
    ): Result<BatchUpdateSpreadsheetResponse> {
        return try {
            logger.d("GoogleSheetsExporter", "Formatting cells in spreadsheet: $spreadsheetId")

            val requests = mutableListOf<Request>()

            // 1. Format header row (row 0) - bold with light gray background
            requests.add(
                Request().setRepeatCell(
                    RepeatCellRequest().apply {
                        range = GridRange().apply {
                            this.sheetId = sheetId
                            startRowIndex = 0
                            endRowIndex = 1 // Just the first row
                        }
                        cell = CellData().apply {
                            userEnteredFormat = CellFormat().apply {
                                // Bold text
                                textFormat = TextFormat().apply {
                                    bold = true
                                }
                                // Light gray background
                                backgroundColor = Color().apply {
                                    red = HEADER_BACKGROUND_RED
                                    green = HEADER_BACKGROUND_GREEN
                                    blue = HEADER_BACKGROUND_BLUE
                                }
                            }
                        }
                        fields = "userEnteredFormat(textFormat,backgroundColor)"
                    }
                )
            )

            // 2. Format date column (column 0) - Date format
            requests.add(
                Request().setRepeatCell(
                    RepeatCellRequest().apply {
                        range = GridRange().apply {
                            this.sheetId = sheetId
                            startColumnIndex = DATE_COLUMN_INDEX
                            endColumnIndex = DATE_COLUMN_INDEX + 1
                            startRowIndex = 1 // Skip header
                        }
                        cell = CellData().apply {
                            userEnteredFormat = CellFormat().apply {
                                numberFormat = NumberFormat().apply {
                                    type = "DATE"
                                    pattern = "yyyy-mm-dd"
                                }
                            }
                        }
                        fields = "userEnteredFormat.numberFormat"
                    }
                )
            )

            // 3. Format amount column (column 3) - Number format with 2 decimal places
            requests.add(
                Request().setRepeatCell(
                    RepeatCellRequest().apply {
                        range = GridRange().apply {
                            this.sheetId = sheetId
                            startColumnIndex = AMOUNT_COLUMN_INDEX
                            endColumnIndex = AMOUNT_COLUMN_INDEX + 1
                            startRowIndex = 1 // Skip header
                        }
                        cell = CellData().apply {
                            userEnteredFormat = CellFormat().apply {
                                numberFormat = NumberFormat().apply {
                                    type = "NUMBER"
                                    pattern = "#,##0.00"
                                }
                            }
                        }
                        fields = "userEnteredFormat.numberFormat"
                    }
                )
            )

            // 4. Auto-resize all columns
            requests.add(
                Request().setAutoResizeDimensions(
                    AutoResizeDimensionsRequest().apply {
                        dimensions = DimensionRange().apply {
                            this.sheetId = sheetId
                            dimension = "COLUMNS"
                            startIndex = 0
                            endIndex = 11 // All 11 columns (0-10)
                        }
                    }
                )
            )

            // Execute batch update
            val batchRequest = BatchUpdateSpreadsheetRequest().apply {
                setRequests(requests)
            }

            val response = sheetsService.spreadsheets()
                .batchUpdate(spreadsheetId, batchRequest)
                .execute()

            logger.i("GoogleSheetsExporter", "Cell formatting applied successfully")
            Result.success(response)

        } catch (e: Exception) {
            logger.e("GoogleSheetsExporter", "Failed to format cells", e)
            Result.failure(e)
        }
    }

    /**
     * Performs complete end-to-end export of transaction data to Google Sheets.
     *
     * This method orchestrates the entire export process:
     * 1. Creates a new spreadsheet
     * 2. Writes the transaction data
     * 3. Applies formatting
     * 4. Returns the shareable URL
     *
     * @param sheetsService The authenticated Sheets service instance
     * @param transactionData List of rows including header row and transaction data
     * @param sheetName Optional custom name for the spreadsheet
     * @return Result containing the spreadsheet URL or exception on failure
     */
    fun exportTransactions(
        sheetsService: Sheets,
        transactionData: List<List<String>>,
        sheetName: String = generateDefaultSheetName()
    ): Result<String> {
        return try {
            logger.i(
                "GoogleSheetsExporter",
                "Starting transaction export: ${transactionData.size - 1} transactions"
            )

            // Step 1: Create spreadsheet
            val createResult = createSpreadsheet(sheetsService, sheetName)
            if (createResult.isFailure) {
                return Result.failure(
                    createResult.exceptionOrNull() ?: Exception("Failed to create spreadsheet")
                )
            }

            val spreadsheet = createResult.getOrNull()
                ?: return Result.failure(Exception("Spreadsheet creation returned null"))

            val spreadsheetId = spreadsheet.spreadsheetId
            val sheetId = spreadsheet.sheets?.firstOrNull()?.properties?.sheetId ?: 0

            // Step 2: Write data
            val writeResult = writeData(sheetsService, spreadsheetId, transactionData)
            if (writeResult.isFailure) {
                logger.w(
                    "GoogleSheetsExporter",
                    "Failed to write data. Created spreadsheet may be empty: $spreadsheetId"
                )
                return Result.failure(
                    writeResult.exceptionOrNull() ?: Exception("Failed to write data")
                )
            }

            // Step 3: Format cells
            val formatResult = formatCells(sheetsService, spreadsheetId, sheetId)
            if (formatResult.isFailure) {
                logger.w(
                    "GoogleSheetsExporter",
                    "Failed to format cells. Data was written but formatting may be incomplete"
                )
                // Don't fail the entire export if formatting fails - data is still there
            }

            // Step 4: Return shareable URL
            val spreadsheetUrl = spreadsheet.spreadsheetUrl
                ?: "https://docs.google.com/spreadsheets/d/$spreadsheetId"

            logger.i(
                "GoogleSheetsExporter",
                "Export completed successfully. URL: $spreadsheetUrl"
            )
            Result.success(spreadsheetUrl)

        } catch (e: Exception) {
            logger.e("GoogleSheetsExporter", "Export failed with exception", e)
            Result.failure(e)
        }
    }

    /**
     * Generates a default sheet name with timestamp.
     *
     * Format: "Transactions Export - yyyy-MM-dd HH:mm:ss"
     *
     * @return Generated sheet name
     */
    private fun generateDefaultSheetName(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US)
        val timestamp = dateFormat.format(Date())
        return "$DEFAULT_SHEET_NAME_PREFIX - $timestamp"
    }
}
