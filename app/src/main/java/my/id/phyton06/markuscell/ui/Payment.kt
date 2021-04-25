package my.id.phyton06.markuscell.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import kotlinx.android.synthetic.main.fragment_payment.*
import my.id.phyton06.markuscell.BuildConfig
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.adapters.RiwayatAdapter
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.RiwayatPembayaranModel
import java.io.*

class Payment : RxBaseFragment() {
    private lateinit var listAdapter: RiwayatAdapter
    private var listData:MutableList<RiwayatPembayaranModel> = mutableListOf()
    lateinit var mContext : View
    lateinit var progressDialog: Dialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mContext = inflater.inflate(R.layout.fragment_payment, container, false)
        return mContext
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener {
            if(Utils.moreThanOne && !Utils.isDashboard)
                RxBus.get().send(Utils.CONTRACT)
            else
                RxBus.get().send(Utils.DASHBOARD)
        }
        val data = Utils.detailContract

        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    dialog.dismiss()
                    val asyncMakePdf = AsyncMakePdf(mContext)
                    asyncMakePdf.execute(listData)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }

        if(data.tenor.toInt() == data.jangkawaktu.toInt() )
            payment_angsuran_utama.text = "-"
        else
            payment_angsuran_utama.text = Utils.convertToRp((data.cicilan.toInt() + data.jumlahdenda.toInt()).toString())

        payment_tenor.text = data.jangkawaktu
        payment_sum_cicilan.text = data.tenor
        payment_sisa_cicilan.text = ""+(data.jangkawaktu.toInt() - data.tenor.toInt())

        payment_kontrak.text = data.kodetransaksi
        payment_angsuran.text = Utils.convertToRp(data.cicilan)
        payment_tempo.text = data.jatuhtempo
        payment_denda.text = Utils.convertToRp(data.jumlahdenda)

        var no = 1
        for(dataPayment in Utils.data_payment){
            listData.add(
                    RiwayatPembayaranModel(
                            "" + Utils.convertToRp(dataPayment.debitkredit),
                            "Angsuran ke-" + no,
                            "" + Utils.convertToRp(dataPayment.denda),
                            "" + dataPayment.tanggalbayar,
                            "" + dataPayment.jatuhtempo,
                            "" + dataPayment.status
                    )
            )
            no++
        }

        Utils.data_payment_history = listData

        listRiwayat.layoutManager = LinearLayoutManager(this.context)
        listAdapter = RiwayatAdapter(this.context!!, listData){

        }
        listRiwayat.adapter = listAdapter
        var status = 0
        //cek pembayaran
        for(dataP in Utils.data_payment_history){
            if(dataP.status.equals("3") && (dataP.no_angsuran == data.jangkawaktu))
                status = 1
        }

        btnPayment.setOnClickListener {
            if(Utils.data_payment_history[Utils.data_payment_history.size-1].status.equals("3")){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Pembayaran terakhir pada nomor kontrak "+Utils.detailContract.kodetransaksi+ " masih dalam proses. Silahkan menunggu hingga proses selesai")
                    .setNegativeButton("OK", dialogClickListener)
                    .setCancelable(false)
                    .show()
            }
            else if((Utils.data_payment_history[Utils.data_payment_history.size-1].status.equals("4") || Utils.data_payment_history[Utils.data_payment_history.size-1].status.equals("2")) && (data.tenor == data.jangkawaktu)){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Nomor kontrak "+Utils.detailContract.kodetransaksi+ " sudah tidak aktif dan sudah lunas.")
                    .setNegativeButton("OK", dialogClickListener)
                    .setCancelable(false)
                    .show()
            }
            else{
                btnPayment.setBackgroundColor(Color.parseColor("#448aff"))
                Utils.tagihan_payment = data.cicilan
                Utils.tagihan_id = data.idPayment.toString()
                RxBus.get().send(Utils.PAYMENT_INFO)
            }
        }

        print.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("Print Riwayat Pembayaran ? ")
                    .setPositiveButton("YA", dialogClickListener)
                    .setNegativeButton("TIDAK", dialogClickListener)
                    .setCancelable(false)
                    .show()
        }
    }

     class AsyncMakePdf(private val mContext: View) : AsyncTask<MutableList<RiwayatPembayaranModel>, String, Int>() {

         lateinit var progressDialog: Dialog
         lateinit var writer: PdfWriter
         var FOLDER_PDF = ""
         var path =""
         val dialog = Dialog(mContext.context)
         val inflate = LayoutInflater.from(mContext.context).inflate(R.layout.loading_alert, null)

         override fun doInBackground(vararg list: MutableList<RiwayatPembayaranModel>?): Int? {

             FOLDER_PDF = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator

             val file: File = File(FOLDER_PDF)
             if (file.exists())
                 file.mkdir()

             val stg = File(FOLDER_PDF, "MarkusCell_" + Utils.detailContract.kodetransaksi + ".pdf")
             path = stg.absolutePath
             Log.d("path", path + "  ==  " + Utils.detailContract.kodetransaksi)

             var baseFont: BaseFont? = null
             try {
                 baseFont = BaseFont.createFont("res/font/lacartoone.ttf", "UTF-8", BaseFont.EMBEDDED)
             } catch (e: IOException) {
                 e.printStackTrace()
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             //create font
             val regularTitle: Font = Font(baseFont, 30f, Font.BOLD, BaseColor.BLACK)
             val regularHead: Font = Font(baseFont, 15f, Font.BOLD, BaseColor(68, 138, 255))
             val regularColor: Font = Font(baseFont, 12f, Font.NORMAL, BaseColor(68, 138, 255))
             val regular: Font = Font(baseFont, 10f, Font.NORMAL, BaseColor.BLACK)
             val rgl: Font = Font(baseFont, 12f, Font.NORMAL, BaseColor.BLACK)
             val regularBold: Font = Font(baseFont, 12f, Font.BOLD, BaseColor.BLACK)
             val regularTabBold: Font = Font(baseFont, 10f, Font.BOLD, BaseColor.BLACK)
             val regularColorBold: Font = Font(baseFont, 12f, Font.BOLD, BaseColor(68, 138, 255))
             val footerN: Font = Font(baseFont, 12f, Font.NORMAL, BaseColor.BLACK)
             val footerE: Font = Font(baseFont, 12f, Font.NORMAL, BaseColor.BLACK)

             //create document
             val document = Document(PageSize.A4, 0f, 0f, 35f, 72f)
             document.addCreationDate()
             document.addAuthor("Komundo")
             document.addCreator("Markus Cell")

             // Location to save
             try {
                 writer = PdfWriter.getInstance(document, FileOutputStream(path))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             } catch (e: FileNotFoundException) {
                 e.printStackTrace()
             }

             //writer.pageEvent = WatermarkPageEvent()

             document.open()

             val preLogo = PdfPCell(Phrase("", regularHead))
             preLogo.horizontalAlignment = Element.ALIGN_RIGHT
             preLogo.verticalAlignment = Element.ALIGN_BOTTOM
             preLogo.border = Rectangle.NO_BORDER

             val preLogo2 = PdfPCell(Phrase("MARKUS CELL", regularHead))
             preLogo2.horizontalAlignment = Element.ALIGN_RIGHT
             preLogo2.verticalAlignment = Element.ALIGN_BOTTOM
             preLogo2.border = Rectangle.NO_BORDER

             val tableHeader = PdfPTable(2)

             try {
                 tableHeader.setWidths(floatArrayOf(1f, 3f))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             try {
                 val d: Drawable = mContext.resources.getDrawable(R.drawable.icon)
                 val bitDw = d as BitmapDrawable
                 val bmp = bitDw.bitmap
                 val stream = ByteArrayOutputStream()
                 bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                 val image = Image.getInstance(stream.toByteArray())
                 image.scaleToFit(20f, 20f)

                 val preImage = PdfPCell(image, true)
                 preImage.border = Rectangle.NO_BORDER

                 tableHeader.addCell(preLogo2)
                 tableHeader.addCell(preLogo)
                 document.add(tableHeader)

             } catch (e: BadElementException) {
                 e.printStackTrace()
             } catch (e: IOException) {
                 e.printStackTrace()
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             val tableTitle = PdfPTable(1)
             val preTitle = PdfPCell(Phrase("Riwayat Pembayaran", regularTitle))

             tableTitle.spacingBefore = 20f
             preTitle.horizontalAlignment = Element.ALIGN_CENTER
             preTitle.border = Rectangle.NO_BORDER

             try {
                 tableTitle.addCell(preTitle)
                 document.add(tableTitle)
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             val tableHeading = PdfPTable(6)
             try {
                 tableHeading.setWidths(floatArrayOf(1.5f, 0.1f, 4f, 1.2f, 0.1f, 2f))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }
             tableHeading.spacingBefore = 25f

             val kontrak = "No. Kontrak"
             val preTitleKontrak = PdfPCell(Phrase(kontrak, rgl))
             val preSepKontrak = PdfPCell(Phrase(":", rgl))
             val preKontrak = PdfPCell(Phrase(" ${Utils.detailContract.kodetransaksi}", regularBold))

             val preTitleTenor = PdfPCell(Phrase("Tenor", rgl))
             val preTenor = PdfPCell(Phrase(" ${Utils.detailContract.jangkawaktu} Bulan", regularBold))

             preSepKontrak.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preKontrak.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preTitleTenor.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preTenor.verticalAlignment = Rectangle.ALIGN_MIDDLE

             preTitleKontrak.border = Rectangle.NO_BORDER
             preSepKontrak.border = Rectangle.NO_BORDER
             preKontrak.border = Rectangle.NO_BORDER
             preTitleTenor.border = Rectangle.NO_BORDER
             preTenor.border = Rectangle.NO_BORDER

             try {
                 tableHeading.addCell(preTitleKontrak)
                 tableHeading.addCell(preSepKontrak)
                 tableHeading.addCell(preKontrak)
                 tableHeading.addCell(preTitleTenor)
                 tableHeading.addCell(preSepKontrak)
                 tableHeading.addCell(preTenor)
                 document.add(tableHeading)
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             val tableHeadingNama = PdfPTable(6)
             try {
                 tableHeadingNama.setWidths(floatArrayOf(1.5f, 0.1f, 4f, 1.2f, 0.1f, 2f))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             var nama = if (Utils.dataProfile.nama == null) "-" else Utils.dataProfile.nama
             if(Utils.dataProfile.sales == 1)
                 nama = if (Utils.detailContract.membername == null) "-" else Utils.detailContract.membername

             val preTitleNama = PdfPCell(Phrase("Nama", rgl))
             val preSepNama = PdfPCell(Phrase(":   ", rgl))
             val preNama = PdfPCell(Phrase(" ${nama}", regularBold))

             val preTitleProd = PdfPCell(Phrase("Harga", rgl))
             val preProd = PdfPCell(Phrase("${Utils.convertToRp(Utils.convertFromJson("jual", "" + Utils.detailContract.idBarang))}", regularBold))

             preTitleNama.border = Rectangle.NO_BORDER
             preSepNama.border = Rectangle.NO_BORDER
             preNama.border = Rectangle.NO_BORDER
             preTitleProd.border = Rectangle.NO_BORDER
             preProd.border = Rectangle.NO_BORDER

             preTitleNama.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preSepNama.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preNama.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preTitleProd.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preProd.verticalAlignment = Rectangle.ALIGN_MIDDLE

             try {
                 tableHeadingNama.addCell(preTitleNama)
                 tableHeadingNama.addCell(preSepNama)
                 tableHeadingNama.addCell(preNama)
                 tableHeadingNama.addCell(preTitleProd)
                 tableHeadingNama.addCell(preSepNama)
                 tableHeadingNama.addCell(preProd)
                 document.add(tableHeadingNama)
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             val tableHeadingNoHp = PdfPTable(6)
             try {
                 tableHeadingNoHp.setWidths(floatArrayOf(1.5f, 0.1f, 4f, 1.2f, 0.1f, 2f))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             var noHp = if (Utils.dataProfile.nohp == null) "-" else Utils.dataProfile.nohp
             if(Utils.dataProfile.sales == 1)
                 noHp = if (Utils.detailContract.memberhp == null) "-" else Utils.detailContract.memberhp

             val preTitleNoHp = PdfPCell(Phrase("Nomor Telp", rgl))
             val preSepNoHp = PdfPCell(Phrase(":   ", rgl))
             val preNoHp = PdfPCell(Phrase(" ${noHp}", regularBold))
             val preTitleProdJum = PdfPCell(Phrase("Jumlah ", rgl))
             val preProdJum = PdfPCell(Phrase("${Utils.convertFromJson("jmlproduct", "" + Utils.detailContract.idBarang)} Unit", regularBold))

             preTitleNoHp.border = Rectangle.NO_BORDER
             preSepNoHp.border = Rectangle.NO_BORDER
             preNoHp.border = Rectangle.NO_BORDER
             preTitleProdJum.border = Rectangle.NO_BORDER
             preProdJum.border = Rectangle.NO_BORDER

             preTitleNoHp.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preSepNoHp.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preProdJum.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preTitleProdJum.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preNoHp.verticalAlignment = Rectangle.ALIGN_MIDDLE

             try {
                 tableHeadingNoHp.addCell(preTitleNoHp)
                 tableHeadingNoHp.addCell(preSepNoHp)
                 tableHeadingNoHp.addCell(preNoHp)
                 tableHeadingNoHp.addCell(preTitleProdJum)
                 tableHeadingNoHp.addCell(preSepNoHp)
                 tableHeadingNoHp.addCell(preProdJum)
                 document.add(tableHeadingNoHp)
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             val tableHeadingAlamat = PdfPTable(6)
             try {
                 tableHeadingAlamat.setWidths(floatArrayOf(1.5f, 0.1f, 4f, 1.2f, 0.1f, 2f))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             var sales = "-"

             if(Utils.dataProfile.sales == 1)
                 sales = Utils.dataProfile.nama

             val preTitleAlamat = PdfPCell(Phrase("Nama Sales", rgl))
             val preSepAlamat = PdfPCell(Phrase(":   ", rgl))
             val preAlamat = PdfPCell(Phrase(" ${sales}", regularBold))
             val preTitleSales = PdfPCell(Phrase("Produk", rgl))
             val preSales = PdfPCell(Phrase("${Utils.convertFromJson("product", "" + Utils.detailContract.idBarang)}", regularBold))

             preTitleAlamat.border = Rectangle.NO_BORDER
             preSepAlamat.border = Rectangle.NO_BORDER
             preAlamat.border = Rectangle.NO_BORDER
             preSales.border = Rectangle.NO_BORDER
             preTitleSales.border = Rectangle.NO_BORDER

             preTitleAlamat.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preSepAlamat.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preAlamat.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preSales.verticalAlignment = Rectangle.ALIGN_MIDDLE
             preSales.verticalAlignment = Rectangle.ALIGN_MIDDLE

             try {
                 tableHeadingAlamat.addCell(preTitleAlamat)
                 tableHeadingAlamat.addCell(preSepAlamat)
                 tableHeadingAlamat.addCell(preAlamat)
                 tableHeadingAlamat.addCell(preTitleSales)
                 tableHeadingAlamat.addCell(preSepAlamat)
                 tableHeadingAlamat.addCell(preSales)
                 document.add(tableHeadingAlamat)
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             val table = PdfPTable(7)
             table.spacingBefore = 10f
             try {
                 table.setWidths(floatArrayOf(0.8f, 2.3f, 2.3f, 2f, 2f, 2f, 1.8f))
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }
             table.headerRows = 1

             table.isSplitRows = false
             table.isComplete = false

             val headNo = PdfPCell(Phrase("NO", regularTabBold))
             val headKet = PdfPCell(Phrase("KET", regularTabBold))
             val headAngs = PdfPCell(Phrase("ANGSURAN", regularTabBold))
             val headDenda = PdfPCell(Phrase("DENDA", regularTabBold))
             val headTempo = PdfPCell(Phrase("JATUH TEMPO", regularTabBold))
             val headBayar = PdfPCell(Phrase("TGL BAYAR", regularTabBold))
             val headStatus = PdfPCell(Phrase("STATUS", regularTabBold))

             headNo.paddingTop = 10f
             headNo.paddingBottom = 10f
             headNo.horizontalAlignment = Element.ALIGN_CENTER

             headKet.paddingTop = 10f
             headKet.paddingBottom = 10f
             headKet.horizontalAlignment = Element.ALIGN_CENTER

             headAngs.paddingTop = 10f
             headAngs.paddingBottom = 10f
             headAngs.horizontalAlignment = Element.ALIGN_CENTER

             headDenda.paddingTop = 10f
             headDenda.paddingBottom = 10f
             headDenda.horizontalAlignment = Element.ALIGN_CENTER

             headTempo.paddingTop = 10f
             headTempo.paddingBottom = 10f
             headTempo.horizontalAlignment = Element.ALIGN_CENTER

             headBayar.paddingTop = 10f
             headBayar.paddingBottom = 10f
             headBayar.horizontalAlignment = Element.ALIGN_CENTER

             headStatus.paddingTop = 10f
             headStatus.paddingBottom = 10f
             headStatus.horizontalAlignment = Element.ALIGN_CENTER

             headNo.backgroundColor = BaseColor(68, 138, 255)
             headKet.backgroundColor = BaseColor(68, 138, 255)
             headAngs.backgroundColor = BaseColor(68, 138, 255)
             headDenda.backgroundColor = BaseColor(68, 138, 255)
             headTempo.backgroundColor = BaseColor(68, 138, 255)
             headBayar.backgroundColor = BaseColor(68, 138, 255)
             headStatus.backgroundColor = BaseColor(68, 138, 255)

             table.addCell(headNo)
             table.addCell(headKet)
             table.addCell(headAngs)
             table.addCell(headDenda)
             table.addCell(headTempo)
             table.addCell(headBayar)
             table.addCell(headStatus)

             val data = Utils.data_payment_history
             var bayar = ""
             var status = ""

             try {
                 for (aw in 0 until data.size) {

                     if(data[aw].tgl_pembayaran.equals("null"))
                         bayar = "-"
                     else
                         bayar = Utils.dateConvert2(data[aw].tgl_pembayaran)

                     if(data[aw].status.equals("2"))
                         status = "Sukses"
                     else if(data[aw].status.equals("3"))
                         status = "Proses"
                     else if(data[aw].status.equals("4"))
                        status = "Gratis"
                     else
                         status = "-"

                     val cellNo = PdfPCell(Phrase((aw + 1).toString(), regular))
                     val cellKet = PdfPCell(Phrase(data[aw].no_angsuran, regular))
                     val cellAngs = PdfPCell(Phrase(data[aw].cicilan, regular))
                     val cellDenda = PdfPCell(Phrase(data[aw].denda, regular))
                     val cellTempo = PdfPCell(Phrase(Utils.dateConvert2(data[aw].tgl_jatuh_tempo), regular))
                     val cellBayar = PdfPCell(Phrase(bayar, regular))
                     val cellStatus = PdfPCell(Phrase(status, regular))

                     cellNo.paddingBottom = 8f
                     cellNo.paddingTop = 8f
                     cellNo.horizontalAlignment = Element.ALIGN_CENTER
                     cellKet.paddingBottom = 8f
                     cellKet.paddingTop = 8f
                     cellKet.horizontalAlignment = Element.ALIGN_CENTER
                     cellAngs.paddingBottom = 8f
                     cellAngs.paddingTop = 8f
                     cellAngs.horizontalAlignment = Element.ALIGN_CENTER
                     cellDenda.paddingBottom = 8f
                     cellDenda.paddingTop = 8f
                     cellDenda.horizontalAlignment = Element.ALIGN_CENTER
                     cellTempo.paddingBottom = 8f
                     cellTempo.paddingTop = 8f
                     cellTempo.horizontalAlignment = Element.ALIGN_CENTER
                     cellBayar.paddingBottom = 8f
                     cellBayar.paddingTop = 8f
                     cellBayar.horizontalAlignment = Element.ALIGN_CENTER
                     cellStatus.paddingBottom = 8f
                     cellStatus.paddingTop = 8f
                     cellStatus.horizontalAlignment = Element.ALIGN_CENTER

                     if (aw % 2 == 0) {
                         cellNo.backgroundColor = BaseColor.WHITE
                         cellKet.backgroundColor = BaseColor.WHITE
                         cellAngs.backgroundColor = BaseColor.WHITE
                         cellDenda.backgroundColor = BaseColor.WHITE
                         cellTempo.backgroundColor = BaseColor.WHITE
                         cellBayar.backgroundColor = BaseColor.WHITE
                         cellStatus.backgroundColor = BaseColor.WHITE
                     } else {
                         cellNo.backgroundColor = BaseColor.LIGHT_GRAY
                         cellKet.backgroundColor = BaseColor.LIGHT_GRAY
                         cellAngs.backgroundColor = BaseColor.LIGHT_GRAY
                         cellDenda.backgroundColor = BaseColor.LIGHT_GRAY
                         cellTempo.backgroundColor = BaseColor.LIGHT_GRAY
                         cellBayar.backgroundColor = BaseColor.LIGHT_GRAY
                         cellStatus.backgroundColor = BaseColor.LIGHT_GRAY
                     }
                     table.addCell(cellNo)
                     table.addCell(cellKet)
                     table.addCell(cellAngs)
                     table.addCell(cellDenda)
                     table.addCell(cellTempo)
                     table.addCell(cellBayar)
                     table.addCell(cellStatus)
                 }
             } catch (e: IndexOutOfBoundsException) {
             }

             val preBorderGray = PdfPCell(Phrase(""))
             preBorderGray.paddingTop = 10f
             preBorderGray.minimumHeight = 20f
             preBorderGray.isUseVariableBorders = true
             preBorderGray.border = Rectangle.BOTTOM
             preBorderGray.borderColorBottom = BaseColor.GRAY
             preBorderGray.borderWidthBottom = 3f
             preBorderGray.colspan = 5

             table.addCell(preBorderGray)

             table.isComplete = true
             try {
                 document.add(table)
             } catch (e: DocumentException) {
                 e.printStackTrace()
             }

             document.close()
             return 1
        }

         override fun onPreExecute() {
             dialog.setContentView(inflate)
             dialog.setCancelable(false)
             dialog.window!!.setBackgroundDrawable(
                     ColorDrawable(Color.TRANSPARENT)
             )
             dialog.show()
         }


         override fun onPostExecute(integer: Int?) {
             super.onPostExecute(integer)
             val file: File = File(path)
             val pdfURI = FileProvider.getUriForFile(mContext.context, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
             val target = Intent(Intent.ACTION_VIEW)
             target.setDataAndType(pdfURI, "application/pdf")
             target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
             target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
             dialog.dismiss()

             val intent = Intent.createChooser(target, "Open File")
             try {
                 (mContext.context).startActivity(intent)
             } catch (e: ActivityNotFoundException) {
                 // Instruct the user to install a PDF reader here, or something
             }
         }
     }

    class WatermarkPageEvent : PdfPageEventHelper() {

        var baseFont: BaseFont = BaseFont.createFont("res/font/lacartoone.ttf", "UTF-8", BaseFont.EMBEDDED)
        //create font
        val FONT: Font = Font(Font.FontFamily.HELVETICA, 70f, Font.BOLD, GrayColor(0.85f))

        override fun onEndPage(writer: PdfWriter, document: Document?) {

            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 50f, 0f)
           /* //template.fill()
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 150f, 0f)
            //template.fill()
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 250f, 0f)
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 350f, 0f)
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 450f, 0f)
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 650f, 0f)
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 750f, 0f)
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 850f, 0f)
            ColumnText.showTextAligned(writer.directContentUnder,
                    Element.ALIGN_CENTER, Phrase("MARKUS CELL", FONT),
                    300f, 950f, 0f)*/

        }

    }




   /* internal class PDFBackground(private val mContext: View) : PdfPageEventHelper() {

        override fun onEndPage(writer: PdfWriter, document: Document) {
            val d: Drawable = mContext.resources.getDrawable(R.drawable.icon)
            val bitDw = d as BitmapDrawable
            val bmp = bitDw.bitmap
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val background = Image.getInstance(stream.toByteArray())
            // This scales the image to the page,
            // use the image's width & height if you don't want to scale.
            val width = 200
            val height = 200
            background.setAbsolutePosition(document.pageSize.width/2,document.pageSize.height/2)
            writer.directContentUnder
                    .addImage(background, width.toDouble(), 0.0, 0.0, height.toDouble(), 0.0, 0.0)
        }
    }*/


}